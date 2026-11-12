using System.ComponentModel.DataAnnotations;
using System.Security.Cryptography;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.Models;

namespace SkinnerCafe.API.Controllers;

[ApiController]
[Route("api/orders")]
public class OrdersController : ControllerBase
{
    private readonly SkinnerCafeDbContext _db;
    private readonly UserManager<ApplicationUser> _userManager;

    public OrdersController(
        SkinnerCafeDbContext db,
        UserManager<ApplicationUser> userManager)
    {
        _db = db;
        _userManager = userManager;
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

    [HttpPost]
    [AllowAnonymous]
    public async Task<IActionResult> CreateOrder(
        CreateOrderRequest request)
    {
        if (!ModelState.IsValid)
            return ValidationProblem(ModelState);

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
                    Notes = "Order placed through Android app.",
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
        catch (DbUpdateException)
        {
            await transaction.RollbackAsync();

            return Problem(
                title: "Order could not be saved.",
                statusCode: StatusCodes.Status500InternalServerError);
        }
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
