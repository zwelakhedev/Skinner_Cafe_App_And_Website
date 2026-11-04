using Microsoft.AspNetCore.Hosting;

namespace SkinnerCafe.API.Services;

public class ProfilePictureService
{
    private readonly IWebHostEnvironment _environment;

    public ProfilePictureService(IWebHostEnvironment environment)
    {
        _environment = environment;
    }

    private const long MaxFileSize = 5 * 1024 * 1024;

    private static readonly Dictionary<string, string> AllowedImageTypes =
        new()
        {
            { "image/jpeg", ".jpg" },
            { "image/png", ".png" },
            { "image/webp", ".webp" }
        };

    public async Task<string> SaveAsync(IFormFile file)
    {
        if (file.Length == 0 || file.Length > MaxFileSize)
        {
            throw new InvalidOperationException(
                "The image must be between 1 byte and 5 MB.");
        }

        if (!AllowedImageTypes.TryGetValue(
            file.ContentType.ToLowerInvariant(), out var extension))
        {
            throw new InvalidOperationException(
                "Only JPEG, PNG and WebP images are supported.");
        }

        var uploadsFolder = Path.Combine(
            _environment.WebRootPath ?? Path.Combine(
                _environment.ContentRootPath, "wwwroot"),
            "uploads",
            "profiles");

        Directory.CreateDirectory(uploadsFolder);

        var fileName = $"{Guid.NewGuid():N}{extension}";
        var filePath = Path.Combine(uploadsFolder, fileName);

        await using var stream = new FileStream(filePath, FileMode.CreateNew);
        await file.CopyToAsync(stream);

        return $"/uploads/profiles/{fileName}";
    }

    public void Delete(string? pictureUrl)
    {
        if (string.IsNullOrWhiteSpace(pictureUrl))
        {
            return;
        }

        var fileName = Path.GetFileName(pictureUrl);

        if (string.IsNullOrWhiteSpace(fileName))
        {
            return;
        }

        var filePath = Path.Combine(
            _environment.WebRootPath ?? Path.Combine(
                _environment.ContentRootPath, "wwwroot"),
            "uploads",
            "profiles",
            fileName);

        if (File.Exists(filePath))
        {
            File.Delete(filePath);
        }
    }
}