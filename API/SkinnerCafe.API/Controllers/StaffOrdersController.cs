using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.DTOs;
using SkinnerCafe.API.Models;
using SkinnerCafe.API.Services.Notifications;

namespace SkinnerCafe.API.Controllers;

[ApiController]
[Route("api/staff/orders")]
[Authorize(Roles = "Staff,Admin")]
public class StaffOrdersController : ControllerBase
{
    private readonly SkinnerCafeDbContext _db;
    private readonly UserManager<ApplicationUser> _userManager;
    private readonly IOrderNotificationService _notificationService;

    public StaffOrdersController(
        SkinnerCafeDbContext db,
        UserManager<ApplicationUser> userManager,
        IOrderNotificationService notificationService)
    {
        _db = db;
        _userManager = userManager;
        _notificationService = notificationService;
    }

    private static readonly IReadOnlyDictionary<string, string[]> AllowedTransitions =
        new Dictionary<string, string[]>
        {
            ["Pending"] = ["Confirmed", "Cancelled"],
            ["Confirmed"] = ["Preparing", "Cancelled"],
            ["Preparing"] = ["Ready", "Cancelled"],
            ["Ready"] = ["Collected"],
            ["Collected"] = ["Completed"]
        };

    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<StaffOrderResponse>>> GetOrders(
    CancellationToken cancellationToken)
    {
        var orders = await _db.Orders
            .AsNoTracking()
            .OrderByDescending(o => o.CreatedAt)
            .Select(o => new StaffOrderResponse(
                o.OrderId,
                o.OrderNumber,
                o.OrderSource,
                o.OrderStatus,
                o.PaymentStatus,
                o.CustomerName,
                o.CustomerEmail,
                o.CustomerPhone,
                o.Subtotal,
                o.DiscountAmount,
                o.TotalAmount,
                o.CreatedAt,
                o.CompletedAt,

                o.OrderItems
                    .OrderBy(i => i.OrderItemId)
                    .Select(i => new StaffOrderItemResponse(
                        i.OrderItemId,
                        i.ProductNameSnapshot,
                        i.VariantNameSnapshot,
                        i.Quantity,
                        i.UnitPrice,
                        i.LineTotal,
                        i.OrderItemAddOns
                            .OrderBy(a => a.OrderItemAddOnId)
                            .Select(a => new StaffOrderAddOnResponse(
                                a.AddOnNameSnapshot,
                                a.Quantity,
                                a.UnitPrice
                            ))
                            .ToList()
                    ))
                    .ToList(),

                o.Payments
                    .OrderByDescending(p => p.CreatedAt)
                    .Select(p => new StaffOrderPaymentResponse(
                        p.PaymentId,
                        p.PaymentMethod,
                        p.PaymentStatus,
                        p.Amount,
                        p.PaymentProvider,
                        p.CreatedAt,
                        p.PaidAt
                    ))
                    .ToList(),

                o.OrderStatusHistories
                    .OrderBy(h => h.ChangedAt)
                    .Select(h => new StaffOrderStatusHistoryResponse(
                        h.PreviousStatus,
                        h.NewStatus,
                        h.ChangedByIdentityUserId,
                        h.Notes,
                        h.ChangedAt
                    ))
                    .ToList()
            ))
            .ToListAsync(cancellationToken);

        return Ok(orders);
    }

    [HttpPut("{orderId:int}/status")]
    public async Task<IActionResult> UpdateOrderStatus(
        int orderId,
        [FromBody] UpdateOrderStatusRequest request,
        CancellationToken cancellationToken)
    {
        var staffUser = await _userManager.GetUserAsync(User);

        if (staffUser is null)
        {
            return Unauthorized();
        }

        var order = await _db.Orders
            .Include(o => o.Customer)
            .FirstOrDefaultAsync(
                o => o.OrderId == orderId,
                cancellationToken);

        if (order is null)
        {
            return NotFound("Order not found.");
        }

        var currentStatus = order.OrderStatus;
        var requestedStatus = request.Status.Trim();

        if (!AllowedTransitions.TryGetValue(
                currentStatus,
                out var allowedStatuses) ||
            !allowedStatuses.Contains(
                requestedStatus,
                StringComparer.OrdinalIgnoreCase))
        {
            return BadRequest(
                $"Order cannot transition from {currentStatus} to {requestedStatus}.");
        }

        // Use the canonical status spelling from the transition rules.
        requestedStatus = allowedStatuses.First(
            status => status.Equals(
                requestedStatus,
                StringComparison.OrdinalIgnoreCase));

        var now = DateTime.UtcNow;

        order.OrderStatus = requestedStatus;

        if (requestedStatus == "Completed")
        {
            order.CompletedAt = now;
        }

        _db.OrderStatusHistories.Add(new OrderStatusHistory
        {
            OrderId = order.OrderId,
            PreviousStatus = currentStatus,
            NewStatus = requestedStatus,
            ChangedByIdentityUserId = staffUser.Id,
            Notes = string.IsNullOrWhiteSpace(request.Notes)
                ? null
                : request.Notes.Trim(),
            ChangedAt = now
        });

        await _db.SaveChangesAsync(cancellationToken);

        // Notify only account-linked customers after the status is saved.
        if (
            requestedStatus is "Confirmed" or "Ready" &&
            order.Customer is { IsActive: true }
        )
        {
            await _notificationService.SendOrderStatusNotificationAsync(
                order.Customer.IdentityUserId,
                order.OrderId.ToString(),
                order.OrderNumber,
                requestedStatus,
                cancellationToken);
        }

        return Ok(new
        {
            order.OrderId,
            order.OrderNumber,
            PreviousStatus = currentStatus,
            order.OrderStatus,
            order.PaymentStatus,
            order.CompletedAt
        });
    }
}
