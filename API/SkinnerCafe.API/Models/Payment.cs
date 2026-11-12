using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class Payment
{
    public long PaymentId { get; set; }

    public int OrderId { get; set; }

    public string PaymentMethod { get; set; } = null!;

    public string PaymentStatus { get; set; } = null!;

    public decimal Amount { get; set; }

    public string? PaymentProvider { get; set; }

    public string? ProviderReference { get; set; }

    public string? IdempotencyKey { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime? PaidAt { get; set; }

    public virtual Order Order { get; set; } = null!;
}

