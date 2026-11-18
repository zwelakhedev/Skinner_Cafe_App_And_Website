using System.Security.Cryptography;
using Microsoft.Data.SqlClient;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.DTOs;
using SkinnerCafe.API.Models;

namespace SkinnerCafe.API.Controllers;

[ApiController]
[Route("api/orders")]
public class PaymentsController : ControllerBase
{
    private readonly SkinnerCafeDbContext _db;
    private readonly UserManager<ApplicationUser> _userManager;

    public PaymentsController(
        SkinnerCafeDbContext db,
        UserManager<ApplicationUser> userManager)
    {
        _db = db;
        _userManager = userManager;
    }

    [HttpPost("{orderId:int}/simulate-payment")]
    [AllowAnonymous]
    public async Task<IActionResult> SimulatePayment(
        int orderId,
        SimulatePaymentRequest request)
    {
        if (!ModelState.IsValid)
            return ValidationProblem(ModelState);

        var paymentMethod = request.PaymentMethod.Trim();
        var paymentProvider = request.PaymentProvider.Trim();
        var outcome = request.SimulationOutcome.Trim();

        if (!IsValidProviderForMethod(paymentMethod, paymentProvider))
        {
            return BadRequest(new
            {
                message = "The selected payment provider is not valid for this payment method."
            });
        }

        if (!IsValidOutcome(outcome))
        {
            return BadRequest(new
            {
                message = "The simulation outcome must be Success, Failure or Cancelled."
            });
        }

        outcome = NormalizeOutcome(outcome);

        var order = await _db.Orders
            .AsNoTracking()
            .SingleOrDefaultAsync(o => o.OrderId == orderId);

        if (order is null)
            return NotFound(new { message = "Order not found." });

        // Verify customer ownership or guest access.
        string? identityUserId = null;

        if (order.CustomerId.HasValue)
        {
            if (User.Identity?.IsAuthenticated != true)
                return Unauthorized();

            var user = await _userManager.GetUserAsync(User);

            if (user is null)
                return Unauthorized();

            var customer = await _db.CustomerProfiles
                .SingleOrDefaultAsync(c =>
                    c.IdentityUserId == user.Id &&
                    c.IsActive);

            if (customer is null ||
                customer.CustomerId != order.CustomerId.Value)
            {
                return NotFound(new
                {
                    message = "Order not found."
                });
            }

            identityUserId = customer.IdentityUserId;
        }
        else
        {
            if (!IsValidGuestToken(
                    request.GuestAccessToken,
                    order.GuestAccessTokenHash))
            {
                return Unauthorized(new
                {
                    message = "Invalid guest access token."
                });
            }
        }

        var idempotencyKey = request.IdempotencyKey.Trim();

        var now = DateTime.UtcNow;

        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        try
        {
            // A repeated request with the same key returns its original attempt.
            var existingAttempt = await _db.Payments
                .AsNoTracking()
                .SingleOrDefaultAsync(p =>
                    p.OrderId == orderId &&
                    p.IdempotencyKey == idempotencyKey);

            if (existingAttempt is not null)
            {
                await transaction.CommitAsync();

                return await ReturnPaymentAttempt(
                    orderId,
                    existingAttempt,
                    "This payment attempt has already been processed.");
            }

            // An already-paid order must never be charged again.
            if (order.PaymentStatus == "Successful")
            {
                await transaction.CommitAsync();

                return await ReturnExistingPayment(orderId);
            }

            // Failed attempts may be retried.
            if ((order.PaymentStatus != "Pending" &&
                 order.PaymentStatus != "Failed") ||
                (order.OrderStatus != "Pending" &&
                 order.OrderStatus != "Confirmed"))
            {
                await transaction.RollbackAsync();

                return Conflict(new
                {
                    message = "This order cannot be paid in its current state."
                });
            }

            // Generate a reference for this simulated provider attempt.
            var providerReference =
                "SIM-" + Convert.ToHexString(
                    RandomNumberGenerator.GetBytes(12));

            // Payment status is tracked independently from order status.
            // Failure and cancellation do not change the order status..
            var newOrderPaymentStatus = outcome switch
            {
                "Success" => "Successful",
                "Failure" => "Failed",
                "Cancelled" => "Pending",
                _ => throw new InvalidOperationException(
                    "Unsupported simulation outcome.")
            };

            var claimed = await _db.Orders
                .Where(o =>
                    o.OrderId == orderId &&
                    (o.OrderStatus == "Pending" ||
                     o.OrderStatus == "Confirmed") &&
                    (o.PaymentStatus == "Pending" ||
                     o.PaymentStatus == "Failed"))
                .ExecuteUpdateAsync(setters =>
                    setters.SetProperty(
                        o => o.PaymentStatus,
                        newOrderPaymentStatus));

            if (claimed == 0)
            {
                await transaction.RollbackAsync();

                var latestStatus = await _db.Orders
                    .AsNoTracking()
                    .Where(o => o.OrderId == orderId)
                    .Select(o => o.PaymentStatus)
                    .SingleOrDefaultAsync();

                if (latestStatus == "Successful")
                    return await ReturnExistingPayment(orderId);

                return Conflict(new
                {
                    message = "The order is no longer awaiting payment."
                });
            }

            var paidOrder = await _db.Orders
                .Include(o => o.OrderItems)
                    .ThenInclude(i => i.Variant)
                        .ThenInclude(v => v.Product)
                .SingleAsync(o => o.OrderId == orderId);

            var payment = new Payment
            {
                OrderId = orderId,
                PaymentMethod = paymentMethod,
                PaymentProvider = paymentProvider,
                PaymentStatus = outcome switch
                {
                    "Success" => "Successful",
                    "Failure" => "Failed",
                    "Cancelled" => "Cancelled",
                    _ => throw new InvalidOperationException()
                },
                Amount = paidOrder.TotalAmount,
                ProviderReference = providerReference,
                IdempotencyKey = idempotencyKey,
                CreatedAt = now,
                PaidAt = outcome == "Success" ? now : null
            };

            _db.Payments.Add(payment);

            // Inventory is only affected after successful payment.
            if (outcome == "Success")
            {
                var stockRequirements = paidOrder.OrderItems
                    .Where(i => i.Variant.Product.IsStockTracked)
                    .GroupBy(i => i.Variant.ProductId)
                    .Select(group => new
                    {
                        ProductId = group.Key,
                        ProductName = group.First()
                            .Variant.Product.ProductName,
                        Quantity = group.Sum(i => i.Quantity)
                    })
                    .OrderBy(item => item.ProductId)
                    .ToList();

                foreach (var item in stockRequirements)
                {
                    var updatedRows = await _db.ProductInventories
                        .Where(inventory =>
                            inventory.ProductId == item.ProductId &&
                            inventory.QuantityOnHand >= item.Quantity)
                        .ExecuteUpdateAsync(setters => setters
                            .SetProperty(
                                inventory => inventory.QuantityOnHand,
                                inventory =>
                                    inventory.QuantityOnHand - item.Quantity)
                            .SetProperty(
                                inventory => inventory.UpdatedAt,
                                now));

                    if (updatedRows != 1)
                    {
                        await transaction.RollbackAsync();

                        return Conflict(new
                        {
                            message =
                                $"Insufficient stock for {item.ProductName}. " +
                                "The payment was not completed."
                        });
                    }

                    _db.StockMovements.Add(new StockMovement
                    {
                        ProductId = item.ProductId,
                        MovementType = "Sale",
                        QuantityChange = -item.Quantity,
                        OrderId = orderId,
                        ChangedByIdentityUserId = identityUserId,
                        Reason =
                            $"Stock deducted after successful simulated {paymentProvider} payment.",
                        CreatedAt = now
                    });
                }
            }

            await _db.SaveChangesAsync();
            await transaction.CommitAsync();

            var message = outcome switch
            {
                "Success" =>
                    "Simulated payment successful.",

                "Failure" =>
                    "Simulated payment failed. You can try another payment method.",

                "Cancelled" =>
                    "Simulated payment was cancelled. You can try again.",

                _ => "Payment processed."
            };

            return Ok(new
            {
                message,

                payment = new
                {
                    payment.PaymentId,
                    payment.PaymentMethod,
                    payment.PaymentProvider,
                    payment.PaymentStatus,
                    payment.Amount,
                    payment.ProviderReference,
                    payment.PaidAt
                },

                order = new
                {
                    paidOrder.OrderId,
                    paidOrder.OrderNumber,
                    paidOrder.OrderStatus,
                    paidOrder.PaymentStatus
                }
            });
        }
        catch (DbUpdateException ex)
        {
            await transaction.RollbackAsync();

            // SQL Server error 2601: duplicate index key.
            // SQL Server error 2627: unique constraint violation.
            var sqlException = ex.GetBaseException() as SqlException;

            if (sqlException is not null &&
                (sqlException.Number == 2601 ||
                 sqlException.Number == 2627))
            {
                // Check whether another request saved this exact attempt.
                var existingAttempt = await _db.Payments
                    .AsNoTracking()
                    .SingleOrDefaultAsync(p =>
                        p.OrderId == orderId &&
                        p.IdempotencyKey == idempotencyKey);

                if (existingAttempt is not null)
                {
                    return await ReturnPaymentAttempt(
                        orderId,
                        existingAttempt,
                        "This payment attempt has already been processed.");
                }
            }

            return Conflict(new
            {
                message =
                    "The payment could not be completed. " +
                    "Please check the order status before trying again."
            });
        }
        catch (Exception)
        {
            await transaction.RollbackAsync();

            // A concurrent request may have saved the same idempotency key.
            var existingAttempt = await _db.Payments
                .AsNoTracking()
                .SingleOrDefaultAsync(p =>
                    p.OrderId == orderId &&
                    p.IdempotencyKey == idempotencyKey);

            if (existingAttempt is not null)
            {
                return await ReturnPaymentAttempt(
                    orderId,
                    existingAttempt,
                    "This payment attempt has already been processed.");
            }

            return Conflict(new
            {
                message =
                    "The payment could not be completed. " +
                    "Please check the order status before trying again."
            });
        }
    }

    private async Task<IActionResult> ReturnPaymentAttempt(
        int orderId,
        Payment payment,
        string message)
    {
        var order = await _db.Orders
            .AsNoTracking()
            .Where(o => o.OrderId == orderId)
            .Select(o => new
            {
                o.OrderId,
                o.OrderNumber,
                o.OrderStatus,
                o.PaymentStatus
            })
            .SingleAsync();

        return Ok(new
        {
            message,

            payment = new
            {
                payment.PaymentId,
                payment.PaymentMethod,
                payment.PaymentProvider,
                payment.PaymentStatus,
                payment.Amount,
                payment.ProviderReference,
                payment.PaidAt
            },

            order
        });
    }

    private async Task<IActionResult> ReturnExistingPayment(int orderId)
    {
        var payment = await _db.Payments
            .AsNoTracking()
            .Where(p =>
                p.OrderId == orderId &&
                p.PaymentStatus == "Successful")
            .OrderByDescending(p => p.CreatedAt)
            .FirstOrDefaultAsync();

        if (payment is null)
        {
            return Conflict(new
            {
                message =
                    "The order is marked as paid, but no successful payment record was found."
            });
        }

        return await ReturnPaymentAttempt(
            orderId,
            payment,
            "This order has already been paid.");
    }

    private static bool IsValidProviderForMethod(
        string method,
        string provider)
    {
        return (method, provider) switch
        {
            ("Card", "Visa") => true,
            ("Card", "Mastercard") => true,

            ("InstantEFT", "Ozow") => true,

            ("QR", "SnapScan") => true,
            ("QR", "Zapper") => true,

            ("BankApp", "CapitecPay") => true,

            ("DigitalWallet", "GooglePay") => true,
            ("DigitalWallet", "ApplePay") => true,

            _ => false
        };
    }

    private static bool IsValidOutcome(string outcome)
    {
        return outcome.Equals(
                   "Success",
                   StringComparison.OrdinalIgnoreCase) ||
               outcome.Equals(
                   "Failure",
                   StringComparison.OrdinalIgnoreCase) ||
               outcome.Equals(
                   "Cancelled",
                   StringComparison.OrdinalIgnoreCase);
    }

    private static string NormalizeOutcome(string outcome)
    {
        if (outcome.Equals(
                "Success",
                StringComparison.OrdinalIgnoreCase))
            return "Success";

        if (outcome.Equals(
                "Failure",
                StringComparison.OrdinalIgnoreCase))
            return "Failure";

        return "Cancelled";
    }

    private static bool IsValidGuestToken(
        string? suppliedToken,
        string? storedHash)
    {
        if (string.IsNullOrWhiteSpace(suppliedToken) ||
            suppliedToken.Length != 64 ||
            string.IsNullOrWhiteSpace(storedHash) ||
            storedHash.Length != 64)
        {
            return false;
        }

        try
        {
            var suppliedHash = SHA256.HashData(
                Convert.FromHexString(suppliedToken));

            var expectedHash =
                Convert.FromHexString(storedHash);

            return CryptographicOperations.FixedTimeEquals(
                suppliedHash,
                expectedHash);
        }
        catch (FormatException)
        {
            return false;
        }
    }
}
