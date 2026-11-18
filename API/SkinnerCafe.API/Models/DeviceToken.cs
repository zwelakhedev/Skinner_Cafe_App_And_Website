namespace SkinnerCafe.API.Models;

public class DeviceToken
{
    public int Id { get; set; }

    public string IdentityUserId { get; set; } = null!;

    public string Token { get; set; } = null!;

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public bool IsActive { get; set; }

    public virtual ApplicationUser User { get; set; } = null!;
}