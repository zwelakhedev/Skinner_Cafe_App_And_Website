namespace SkinnerCafe.API.DTOs;

public record StaffOrderResponse(
    int OrderId,
    string OrderNumber,
    string OrderSource,
    string OrderStatus,
    string PaymentStatus,
    string CustomerName,
    string? CustomerEmail,
    string? CustomerPhone,
    decimal Subtotal,
    decimal DiscountAmount,
    decimal TotalAmount,
    DateTime CreatedAt,
    DateTime? CompletedAt,
    IReadOnlyList<StaffOrderItemResponse> Items,
    IReadOnlyList<StaffOrderPaymentResponse> Payments,
    IReadOnlyList<StaffOrderStatusHistoryResponse> StatusHistory
);

public record StaffOrderItemResponse(
    int OrderItemId,
    string ProductName,
    string VariantName,
    int Quantity,
    decimal UnitPrice,
    decimal LineTotal,
    IReadOnlyList<StaffOrderAddOnResponse> AddOns
);

public record StaffOrderAddOnResponse(
    string Name,
    int Quantity,
    decimal UnitPrice
);

public record StaffOrderPaymentResponse(
    long PaymentId,
    string PaymentMethod,
    string PaymentStatus,
    decimal Amount,
    string? PaymentProvider,
    DateTime CreatedAt,
    DateTime? PaidAt
);

public record StaffOrderStatusHistoryResponse(
    string? PreviousStatus,
    string NewStatus,
    string? ChangedByIdentityUserId,
    string? Notes,
    DateTime ChangedAt
);
