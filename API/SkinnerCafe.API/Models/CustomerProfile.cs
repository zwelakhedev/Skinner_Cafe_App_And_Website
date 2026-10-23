using System;
using System.Collections.Generic;

namespace SkinnerCafe.API.Models;

public partial class CustomerProfile
{
    public int CustomerId { get; set; }

    public string IdentityUserId { get; set; } = null!;

    public string FirstName { get; set; } = null!;

    public string LastName { get; set; } = null!;

    public string Email { get; set; } = null!;

    public string? PhoneNumber { get; set; }

    public DateTime CreatedAt { get; set; }

    public bool IsActive { get; set; }

    public virtual ICollection<Order> Orders { get; set; } = new List<Order>();
}
