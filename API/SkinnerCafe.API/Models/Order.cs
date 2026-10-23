using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class Order
{
    public int OrderId { get; set; }

    public string OrderNumber { get; set; } = null!;

    public int? CustomerId { get; set; }

    public string OrderSource { get; set; } = null!;

    public string OrderStatus { get; set; } = null!;

    public string PaymentStatus { get; set; } = null!;

    public string CustomerName { get; set; } = null!;

    public string? CustomerEmail { get; set; }

    public string? CustomerPhone { get; set; }

    public decimal Subtotal { get; set; }

    public decimal DiscountAmount { get; set; }

    public decimal TotalAmount { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime? CompletedAt { get; set; }

    public virtual CustomerProfile? Customer { get; set; }

    public virtual ICollection<OrderItem> OrderItems { get; set; } = new List<OrderItem>();

    public virtual ICollection<OrderStatusHistory> OrderStatusHistories { get; set; } = new List<OrderStatusHistory>();

    public virtual ICollection<Payment> Payments { get; set; } = new List<Payment>();

    public virtual ICollection<StockMovement> StockMovements { get; set; } = new List<StockMovement>();
}
