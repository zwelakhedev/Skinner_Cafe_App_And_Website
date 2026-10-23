using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class ProductInventory
{
    public int ProductId { get; set; }

    public int QuantityOnHand { get; set; }

    public int LowStockThreshold { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual Product Product { get; set; } = null!;
}
