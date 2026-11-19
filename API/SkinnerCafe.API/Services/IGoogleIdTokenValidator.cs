namespace SkinnerCafe.API.Services;

public interface IGoogleIdTokenValidator
{
    Task<GoogleUserInfo?> ValidateAsync(string idToken);
}

public record GoogleUserInfo(
    string Subject,
    string Email,
    string FirstName,
    string LastName
);