using System.ComponentModel.DataAnnotations;

namespace SkinnerCafe.API.DTOs;

public record UpdateOrderStatusRequest(
    [Required, StringLength(20, MinimumLength = 1)]
    string Status,

    [StringLength(300)]
    string? Notes
);
