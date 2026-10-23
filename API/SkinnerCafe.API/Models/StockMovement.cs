using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class StockMovement
{
    public long StockMovementId { get; set; }

    public int ProductId { get; set; }

    public string MovementType { get; set; } = null!;

    public int QuantityChange { get; set; }

    public int? OrderId { get; set; }

    public int? StockDeliveryId { get; set; }

    public int? StockDeliveryItemId { get; set; }

    public string? ChangedByIdentityUserId { get; set; }

    public string? Reason { get; set; }

    public DateTime CreatedAt { get; set; }

    public virtual Order? Order { get; set; }

    public virtual Product Product { get; set; } = null!;

    public virtual StockDelivery? StockDelivery { get; set; }

    public virtual StockDeliveryItem? StockDeliveryItem { get; set; }
}
