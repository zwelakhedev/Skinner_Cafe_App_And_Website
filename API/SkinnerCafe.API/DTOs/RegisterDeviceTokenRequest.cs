using System.ComponentModel.DataAnnotations;

namespace SkinnerCafe.API.DTOs;

public record RegisterDeviceTokenRequest(
    [Required, StringLength(850, MinimumLength = 1)]
    string Token
);

