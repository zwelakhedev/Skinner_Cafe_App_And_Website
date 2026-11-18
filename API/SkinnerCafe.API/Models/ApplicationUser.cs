using Microsoft.AspNetCore.Identity;

namespace SkinnerCafe.API.Models;

public class ApplicationUser : IdentityUser
{
    public virtual ICollection<DeviceToken> DeviceTokens { get; set; }
        = new List<DeviceToken>();
}
