using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class StockDelivery
{
    public int StockDeliveryId { get; set; }

    public string SupplierName { get; set; } = null!;

    public string? DeliveryReference { get; set; }

    public DateTime ReceivedAt { get; set; }

    public string? ReceivedByIdentityUserId { get; set; }

    public string? Notes { get; set; }

    public virtual ICollection<StockDeliveryItem> StockDeliveryItems { get; set; } = new List<StockDeliveryItem>();

    public virtual ICollection<StockMovement> StockMovements { get; set; } = new List<StockMovement>();
}
