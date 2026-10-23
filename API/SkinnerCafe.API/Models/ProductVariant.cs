using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class ProductVariant
{
    public int VariantId { get; set; }

    public int ProductId { get; set; }

    public string VariantName { get; set; } = null!;

    public decimal Price { get; set; }

    public bool IsActive { get; set; }

    public int DisplayOrder { get; set; }

    public virtual ICollection<OrderItem> OrderItems { get; set; } = new List<OrderItem>();

    public virtual Product Product { get; set; } = null!;
}
