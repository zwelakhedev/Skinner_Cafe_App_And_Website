using System.ComponentModel.DataAnnotations;

namespace SkinnerCafe.API.DTOs;

public record SimulatePaymentRequest(
    [StringLength(64, MinimumLength = 64)]
    string? GuestAccessToken,

    [Required, StringLength(100, MinimumLength = 1)]
    string IdempotencyKey,

    [Required, StringLength(20)]
    string PaymentMethod,

    [Required, StringLength(40)]
    string PaymentProvider,

    [Required, StringLength(20)]
    string SimulationOutcome
);

