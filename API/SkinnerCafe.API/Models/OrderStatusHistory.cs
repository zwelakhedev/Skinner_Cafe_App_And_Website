using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class OrderStatusHistory
{
    public long OrderStatusHistoryId { get; set; }

    public int OrderId { get; set; }

    public string? PreviousStatus { get; set; }

    public string NewStatus { get; set; } = null!;

    public string? ChangedByIdentityUserId { get; set; }

    public string? Notes { get; set; }

    public DateTime ChangedAt { get; set; }

    public virtual Order Order { get; set; } = null!;
}
