using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class OrderItem
{
    public int OrderItemId { get; set; }

    public int OrderId { get; set; }

    public int VariantId { get; set; }

    public string ProductNameSnapshot { get; set; } = null!;

    public string VariantNameSnapshot { get; set; } = null!;

    public int Quantity { get; set; }

    public decimal UnitPrice { get; set; }

    public decimal LineTotal { get; set; }

    public virtual Order Order { get; set; } = null!;

    public virtual ICollection<OrderItemAddOn> OrderItemAddOns { get; set; } = new List<OrderItemAddOn>();

    public virtual ProductVariant Variant { get; set; } = null!;
}
