using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class AddOn
{
    public int AddOnId { get; set; }

    public string AddOnName { get; set; } = null!;

    public decimal Price { get; set; }

    public bool IsActive { get; set; }

    public DateTime CreatedAt { get; set; }

    public virtual ICollection<OrderItemAddOn> OrderItemAddOns { get; set; } = new List<OrderItemAddOn>();

    public virtual ICollection<Product> Products { get; set; } = new List<Product>();
}
