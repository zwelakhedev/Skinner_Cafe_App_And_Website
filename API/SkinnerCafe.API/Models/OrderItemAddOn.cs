using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class OrderItemAddOn
{
    public int OrderItemAddOnId { get; set; }

    public int OrderItemId { get; set; }

    public int AddOnId { get; set; }

    public string AddOnNameSnapshot { get; set; } = null!;

    public int Quantity { get; set; }

    public decimal UnitPrice { get; set; }

    public virtual AddOn AddOn { get; set; } = null!;

    public virtual OrderItem OrderItem { get; set; } = null!;
}
