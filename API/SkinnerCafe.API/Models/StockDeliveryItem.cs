using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class StockDeliveryItem
{
    public int StockDeliveryItemId { get; set; }

    public int StockDeliveryId { get; set; }

    public int ProductId { get; set; }

    public int QuantityReceived { get; set; }

    public virtual Product Product { get; set; } = null!;

    public virtual StockDelivery StockDelivery { get; set; } = null!;

    public virtual ICollection<StockMovement> StockMovements { get; set; } = new List<StockMovement>();
}
