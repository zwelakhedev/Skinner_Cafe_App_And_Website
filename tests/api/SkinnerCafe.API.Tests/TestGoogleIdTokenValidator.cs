using SkinnerCafe.API.Services;

namespace SkinnerCafe.API.Tests;

public class TestGoogleIdTokenValidator : IGoogleIdTokenValidator
{
    public Task<GoogleUserInfo?> ValidateAsync(string idToken)
    {
        if (!idToken.StartsWith("valid:"))
            return Task.FromResult<GoogleUserInfo?>(null);

        var email = idToken["valid:".Length..];

        return Task.FromResult<GoogleUserInfo?>(
            new GoogleUserInfo(
                $"google-{email}",
                email,
                "Google",
                "Customer"));
    }
}