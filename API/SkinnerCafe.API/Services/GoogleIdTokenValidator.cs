using Google.Apis.Auth;

namespace SkinnerCafe.API.Services;

public class GoogleIdTokenValidator : IGoogleIdTokenValidator
{
    private readonly IConfiguration _configuration;

    public GoogleIdTokenValidator(IConfiguration configuration)
    {
        _configuration = configuration;
    }

    public async Task<GoogleUserInfo?> ValidateAsync(string idToken)
    {
        var clientId = _configuration["Google:ClientId"];

        if (string.IsNullOrWhiteSpace(clientId))
            return null;

        try
        {
            var payload = await GoogleJsonWebSignature.ValidateAsync(
                idToken,
                new GoogleJsonWebSignature.ValidationSettings
                {
                    Audience = new[] { clientId }
                });

            if (payload.EmailVerified != true ||
                string.IsNullOrWhiteSpace(payload.Subject) ||
                string.IsNullOrWhiteSpace(payload.Email))
                return null;

            return new GoogleUserInfo(
                payload.Subject,
                payload.Email,
                payload.GivenName ?? "",
                payload.FamilyName ?? ""
            );
        }
        catch
        {
            return null;
        }
    }
}