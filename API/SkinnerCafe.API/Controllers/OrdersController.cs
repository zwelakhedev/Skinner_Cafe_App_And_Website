using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.Models;
using System.ComponentModel.DataAnnotations;
using System.Security.Cryptography;

namespace SkinnerCafe.API.Controllers;

[ApiController]
[Route("api/orders")]
public class OrdersController : ControllerBase
{
    private readonly SkinnerCafeDbContext _db;
    private readonly UserManager<ApplicationUser> _userManager;
    private readonly IDataProtector _guestTokenProtector;

    public OrdersController(
        SkinnerCafeDbContext db,
        UserManager<ApplicationUser> userManager,
        IDataProtectionProvider dataProtectionProvider)
    {
        _db = db;
        _userManager = userManager;

        _guestTokenProtector = dataProtectionProvider.CreateProtector(
            "SkinnerCafe.GuestOrderAccessToken.v1");
    }

    public record CreateOrderRequest(
        [Required, StringLength(200)] string CustomerName,
        [EmailAddress, StringLength(256)] string? CustomerEmail,
        [Phone, StringLength(30)] string? CustomerPhone,
        [Required, MinLength(1)] List<CreateOrderItemRequest> Items);

    public record CreateOrderItemRequest(
        [Range(1, int.MaxValue)] int VariantId,
        [Range(1, 100)] int Quantity,
        List<int>? AddOnIds);

    [HttpGet("my-orders")]
    [Authorize]
    public async Task<IActionResult> GetMyOrders()
    {
        var user = await _userManager.GetUserAsync(User);

        if (user is null)
            return Unauthorized();

        var customer = await _db.CustomerProfiles
            .AsNoTracking()
            .SingleOrDefaultAsync(c =>
                c.IdentityUserId == user.Id &&
                c.IsActive);

        if (customer is null)
            return Unauthorized(new
            {
                message = "No active customer profile was found."
            });

        var orders = await _db.Orders
            .AsNoTracking()
            .Where(o => o.CustomerId == customer.CustomerId)
            .Include(o => o.OrderItems)
                .ThenInclude(i => i.OrderItemAddOns)
            .Include(o => o.Payments)
            .Include(o => o.OrderStatusHistories)
            .OrderByDescending(o => o.CreatedAt)
            .ToListAsync();

        var response = orders.Select(order => new
        {
            order.OrderId,
            order.OrderNumber,
            order.OrderSource,
            order.OrderStatus,
            order.PaymentStatus,
            order.Subtotal,
            order.DiscountAmount,
            order.TotalAmount,
            order.CreatedAt,
            order.CompletedAt,

            Items = order.OrderItems.Select(item => new
            {
                item.OrderItemId,
                item.ProductNameSnapshot,
                item.VariantNameSnapshot,
                item.Quantity,
                item.UnitPrice,
                item.LineTotal,

                AddOns = item.OrderItemAddOns.Select(addOn => new
                {
                    addOn.AddOnNameSnapshot,
                    addOn.Quantity,
                    addOn.UnitPrice
                }).ToList()
            }).ToList(),

            Payments = order.Payments
                .OrderByDescending(payment => payment.CreatedAt)
                .Select(payment => new
                {
                    payment.PaymentId,
                    payment.PaymentMethod,
                    payment.PaymentStatus,
                    payment.Amount,
                    payment.PaymentProvider,
                    payment.CreatedAt,
                    payment.PaidAt
                }).ToList(),

            StatusHistory = order.OrderStatusHistories
                .OrderBy(history => history.ChangedAt)
                .Select(history => new
                {
                    history.PreviousStatus,
                    history.NewStatus,
                    history.ChangedAt
                }).ToList()
        }).ToList();

        return Ok(response);
    }
    [HttpPost]
    [AllowAnonymous]
    public async Task<IActionResult> CreateOrder(
        CreateOrderRequest request)
    {
        if (!ModelState.IsValid)
            return ValidationProblem(ModelState);

        if (!Request.Headers.TryGetValue("Idempotency-Key", out var idempotencyHeader) ||
    !Guid.TryParse(idempotencyHeader, out var idempotencyGuid))
        {
            return BadRequest(new
            {
                message = "A valid Idempotency-Key header containing a UUID is required."
            });
        }

        var idempotencyKey = idempotencyGuid.ToString("D");

        CustomerProfile? customer = null;

        if (User.Identity?.IsAuthenticated == true)
        {
            var user = await _userManager.GetUserAsync(User);

            if (user is null)
                return Unauthorized();

            customer = await _db.CustomerProfiles
                .SingleOrDefaultAsync(c =>
                    c.IdentityUserId == user.Id &&
                    c.IsActive);

            if (customer is null)
                return Unauthorized();

            // Authenticated customer details come from their profile.
            // Do not trust customer identity or contact details from the app.
        }

        var customerName = customer is not null
            ? $"{customer.FirstName} {customer.LastName}".Trim()
            : request.CustomerName.Trim();

        var customerEmail = customer?.Email ??
            NormalizeOptional(request.CustomerEmail);

        var customerPhone = customer?.PhoneNumber ??
            NormalizeOptional(request.CustomerPhone);

        if (string.IsNullOrWhiteSpace(customerName))
        {
            return BadRequest(new
            {
                message = "Customer name is required."
            });
        }

        if (customerName.Length > 200 ||
            customerEmail?.Length > 256 ||
            customerPhone?.Length > 30)
        {
            return BadRequest(new
            {
                message = "One or more customer details are too long."
            });
        }

        var isGuest = customer is null;

        var requestFingerprint = CreateRequestFingerprint(
            customer?.CustomerId,
            isGuest,
            customerName,
            customerEmail,
            customerPhone,
            request.Items);

        var existingOrder = await _db.Orders
            .AsNoTracking()
            .SingleOrDefaultAsync(o => o.IdempotencyKey == idempotencyKey);

        if (existingOrder is not null)
        {
            var sameCustomer = existingOrder.CustomerId == customer?.CustomerId;
            var sameRequest = string.Equals(
                existingOrder.RequestFingerprint,
                requestFingerprint,
                StringComparison.Ordinal);

            if (!sameCustomer || !sameRequest)
            {
                return Conflict(new
                {
                    message = "This Idempotency-Key has already been used for a different order request."
                });
            }

            string? originalGuestAccessToken = null;

            if (isGuest)
            {
                if (string.IsNullOrWhiteSpace(existingOrder.GuestAccessTokenProtected))
                {
                    return Problem(
                        title: "The original guest access token could not be recovered.",
                        statusCode: StatusCodes.Status500InternalServerError);
                }

                try
                {
                    originalGuestAccessToken = _guestTokenProtector.Unprotect(
                        existingOrder.GuestAccessTokenProtected);
                }
                catch (CryptographicException)
                {
                    return Problem(
                        title: "The original guest access token could not be recovered.",
                        statusCode: StatusCodes.Status500InternalServerError);
                }
            }

            return Ok(new
            {
                message = "Order already created.",
                guestAccessToken = originalGuestAccessToken,
                order = new
                {
                    existingOrder.OrderId,
                    existingOrder.OrderNumber,
                    existingOrder.OrderStatus,
                    existingOrder.PaymentStatus,
                    existingOrder.Subtotal,
                    existingOrder.DiscountAmount,
                    existingOrder.TotalAmount,
                    existingOrder.CreatedAt
                }
            });
        }

        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        try
        {
            var orderItems = new List<OrderItem>();
            decimal subtotal = 0;

            foreach (var requestedItem in request.Items)
            {
                var variant = await _db.ProductVariants
                    .Include(v => v.Product)
                    .ThenInclude(p => p.AddOns)
                    .SingleOrDefaultAsync(v =>
                        v.VariantId == requestedItem.VariantId &&
                        v.IsActive &&
                        v.Product.IsActive);

                if (variant is null)
                {
                    await transaction.RollbackAsync();

                    return BadRequest(new
                    {
                        message = $"Variant {requestedItem.VariantId} is unavailable."
                    });
                }

                var selectedAddOnIds =
                    requestedItem.AddOnIds ?? new List<int>();

                if (selectedAddOnIds.Any(id => id <= 0) ||
                    selectedAddOnIds.Distinct().Count() !=
                    selectedAddOnIds.Count)
                {
                    await transaction.RollbackAsync();

                    return BadRequest(new
                    {
                        message = "Invalid or duplicate add-ons were selected."
                    });
                }

                var selectedAddOns = variant.Product.AddOns
                    .Where(a =>
                        a.IsActive &&
                        selectedAddOnIds.Contains(a.AddOnId))
                    .ToList();

                if (selectedAddOns.Count != selectedAddOnIds.Count)
                {
                    await transaction.RollbackAsync();

                    return BadRequest(new
                    {
                        message = "One or more selected add-ons are unavailable for this product."
                    });
                }

                var variantPrice = variant.Price;

                var addOnTotal = selectedAddOns.Sum(a => a.Price);

                var unitPrice = variantPrice + addOnTotal;

                var lineTotal = unitPrice * requestedItem.Quantity;

                var orderItem = new OrderItem
                {
                    VariantId = variant.VariantId,
                    ProductNameSnapshot = variant.Product.ProductName,
                    VariantNameSnapshot = variant.VariantName,
                    Quantity = requestedItem.Quantity,
                    UnitPrice = unitPrice,
                    LineTotal = lineTotal,
                    OrderItemAddOns = selectedAddOns
                        .Select(addOn => new OrderItemAddOn
                        {
                            AddOnId = addOn.AddOnId,
                            AddOnNameSnapshot = addOn.AddOnName,
                            Quantity = 1,
                            UnitPrice = addOn.Price
                        })
                        .ToList()
                };

                orderItems.Add(orderItem);
                subtotal += lineTotal;
            }

            string? guestAccessToken = customer is null
                ? Convert.ToHexString(RandomNumberGenerator.GetBytes(32))
                : null;

            string? guestAccessTokenHash = guestAccessToken is null
                ? null
                : Convert.ToHexString(
                    SHA256.HashData(Convert.FromHexString(guestAccessToken)));

            var order = new Order
            {
                OrderNumber = GenerateOrderNumber(),
                CustomerId = customer?.CustomerId,
                OrderSource = "Online",
                OrderStatus = "Pending",
                PaymentStatus = "Pending",
                CustomerName = customerName,
                CustomerEmail = customerEmail,
                CustomerPhone = customerPhone,
                GuestAccessTokenHash = guestAccessTokenHash,
                IdempotencyKey = idempotencyKey,
                RequestFingerprint = requestFingerprint,
                GuestAccessTokenProtected = guestAccessToken is null
                    ? null
                    : _guestTokenProtector.Protect(guestAccessToken),
                Subtotal = subtotal,
                DiscountAmount = 0,
                TotalAmount = subtotal,
                CreatedAt = DateTime.UtcNow,
                OrderItems = orderItems
            };

            order.OrderStatusHistories.Add(
                new OrderStatusHistory
                {
                    PreviousStatus = null,
                    NewStatus = "Pending",
                    ChangedByIdentityUserId = customer?.IdentityUserId,
                    Notes = "Order placed through online ordering.",
                    ChangedAt = DateTime.UtcNow
                });

            _db.Orders.Add(order);

            await _db.SaveChangesAsync();
            await transaction.CommitAsync();

            return StatusCode(StatusCodes.Status201Created, new
            {
                message = "Order created successfully.",
                guestAccessToken,
                order = new
                {
                    order.OrderId,
                    order.OrderNumber,
                    order.OrderStatus,
                    order.PaymentStatus,
                    order.Subtotal,
                    order.DiscountAmount,
                    order.TotalAmount,
                    order.CreatedAt
                }
            });
        }
        catch (DbUpdateException exception)
        {
            await transaction.RollbackAsync();

            if (!IsUniqueConstraintViolation(exception))
            {
                return Problem(
                    title: "Order could not be saved.",
                    statusCode: StatusCodes.Status500InternalServerError);
            }

            // Another request may have created the same order first.
            _db.ChangeTracker.Clear();

            var concurrentOrder = await _db.Orders
                .AsNoTracking()
                .SingleOrDefaultAsync(o => o.IdempotencyKey == idempotencyKey);

            if (concurrentOrder is null)
            {
                return Problem(
                    title: "Order could not be saved.",
                    statusCode: StatusCodes.Status500InternalServerError);
            }

            var sameCustomer =
                concurrentOrder.CustomerId == customer?.CustomerId;

            var sameRequest = string.Equals(
                concurrentOrder.RequestFingerprint,
                requestFingerprint,
                StringComparison.Ordinal);

            if (!sameCustomer || !sameRequest)
            {
                return Conflict(new
                {
                    message = "This Idempotency-Key has already been used for a different order request."
                });
            }

            string? originalGuestAccessToken = null;

            if (isGuest)
            {
                if (string.IsNullOrWhiteSpace(
                    concurrentOrder.GuestAccessTokenProtected))
                {
                    return Problem(
                        title: "The original guest access token could not be recovered.",
                        statusCode: StatusCodes.Status500InternalServerError);
                }

                try
                {
                    originalGuestAccessToken = _guestTokenProtector.Unprotect(
                        concurrentOrder.GuestAccessTokenProtected);
                }
                catch (CryptographicException)
                {
                    return Problem(
                        title: "The original guest access token could not be recovered.",
                        statusCode: StatusCodes.Status500InternalServerError);
                }
            }

            return Ok(new
            {
                message = "Order already created.",
                guestAccessToken = originalGuestAccessToken,
                order = new
                {
                    concurrentOrder.OrderId,
                    concurrentOrder.OrderNumber,
                    concurrentOrder.OrderStatus,
                    concurrentOrder.PaymentStatus,
                    concurrentOrder.Subtotal,
                    concurrentOrder.DiscountAmount,
                    concurrentOrder.TotalAmount,
                    concurrentOrder.CreatedAt
                }
            });
        }
    }

    private static string CreateRequestFingerprint(
    int? customerId,
    bool isGuest,
    string customerName,
    string? customerEmail,
    string? customerPhone,
    List<CreateOrderItemRequest> items)
    {
        var canonicalRequest = new
        {
            CustomerId = customerId,
            CustomerName = customerName,
            IsGuest = isGuest,
            CustomerEmail = customerEmail,
            CustomerPhone = customerPhone,

            Items = items.Select(item => new
            {
                item.VariantId,
                item.Quantity,
                AddOnIds = (item.AddOnIds ?? new List<int>())
                    .OrderBy(id => id)
                    .ToArray()
            }).ToArray()
        };

        var serializedRequest =
            System.Text.Json.JsonSerializer.Serialize(canonicalRequest);

        var hash = SHA256.HashData(
            System.Text.Encoding.UTF8.GetBytes(serializedRequest));

        return Convert.ToHexString(hash);
    }

    private static bool IsUniqueConstraintViolation(
    DbUpdateException exception)
    {
        var innerException = exception.InnerException;

        while (innerException is not null)
        {
            var exceptionType = innerException.GetType();

            // SQL Server: duplicate key errors 2601 and 2627.
            if (exceptionType.FullName ==
                "Microsoft.Data.SqlClient.SqlException")
            {
                var numberProperty = exceptionType.GetProperty("Number");

                if (numberProperty?.GetValue(innerException) is int number &&
                    number is 2601 or 2627)
                {
                    return true;
                }
            }

            // SQLite: constraint violation with unique/primary-key extended codes.
            if (exceptionType.FullName ==
                "Microsoft.Data.Sqlite.SqliteException")
            {
                var errorCodeProperty =
                    exceptionType.GetProperty("SqliteErrorCode");

                var extendedCodeProperty =
                    exceptionType.GetProperty("SqliteExtendedErrorCode");

                var errorCode =
                    errorCodeProperty?.GetValue(innerException) as int?;

                var extendedCode =
                    extendedCodeProperty?.GetValue(innerException) as int?;

                if (errorCode == 19 &&
                    extendedCode is 1555 or 2067)
                {
                    return true;
                }
            }

            innerException = innerException.InnerException;
        }

        return false;
    }

    private static string? NormalizeOptional(string? value)
    {
        return string.IsNullOrWhiteSpace(value)
            ? null
            : value.Trim();
    }

    private static string GenerateOrderNumber()
    {
        var timestamp = DateTime.UtcNow.ToString("yyyyMMddHHmmss");
        var random = Convert.ToHexString(
            RandomNumberGenerator.GetBytes(4));

        return $"SC-{timestamp}-{random}";
    }
}
