using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.Models;
using SkinnerCafe.API.Services;

namespace SkinnerCafe.API.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly UserManager<ApplicationUser> _userManager;
    private readonly SignInManager<ApplicationUser> _signInManager;
    private readonly SkinnerCafeDbContext _db;
    private readonly ProfilePictureService _profilePictureService;

    public AuthController(
        UserManager<ApplicationUser> userManager,
        SignInManager<ApplicationUser> signInManager,
        SkinnerCafeDbContext db,
        ProfilePictureService profilePictureService)
    {
        _userManager = userManager;
        _signInManager = signInManager;
        _db = db;
        _profilePictureService = profilePictureService;
    }

    public record RegisterRequest(
        [Required, StringLength(100)] string FirstName,
        [Required, StringLength(100)] string LastName,
        [Required, EmailAddress, StringLength(256)] string Email,
        [Required, MinLength(8)] string Password,
        [Phone, StringLength(30)] string? PhoneNumber);

    public record LoginRequest(
        [Required, EmailAddress, StringLength(256)] string Email,
        [Required] string Password);

    [HttpPost("register")]
    [AllowAnonymous]
    public async Task<IActionResult> Register(RegisterRequest request)
    {
        var email = request.Email.Trim();

        if (await _userManager.FindByEmailAsync(email) is not null ||
            await _db.CustomerProfiles.AnyAsync(c => c.Email == email))
        {
            return Conflict(new
            {
                message = "An account with this email address already exists."
            });
        }

        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        var user = new ApplicationUser
        {
            UserName = email,
            Email = email,
            PhoneNumber = string.IsNullOrWhiteSpace(request.PhoneNumber)
                ? null
                : request.PhoneNumber.Trim()
        };

        var createUserResult =
            await _userManager.CreateAsync(user, request.Password);

        if (!createUserResult.Succeeded)
        {
            await transaction.RollbackAsync();

            return BadRequest(new
            {
                errors = createUserResult.Errors.Select(e => e.Description)
            });
        }

        var profile = new CustomerProfile
        {
            IdentityUserId = user.Id,
            FirstName = request.FirstName.Trim(),
            LastName = request.LastName.Trim(),
            Email = email,
            PhoneNumber = user.PhoneNumber,
            IsActive = true
        };

        _db.CustomerProfiles.Add(profile);

        try
        {
            await _db.SaveChangesAsync();
            await transaction.CommitAsync();
        }
        catch (DbUpdateException)
        {
            await transaction.RollbackAsync();
            await _userManager.DeleteAsync(user);

            return Conflict(new
            {
                message = "The account could not be created with those details."
            });
        }

        // Automatically sign in the customer after successful registration.
        await _signInManager.SignInAsync(user, isPersistent: false);

        return StatusCode(StatusCodes.Status201Created, new
        {
            message = "Account created successfully.",
            customer = new
            {
                profile.CustomerId,
                profile.FirstName,
                profile.LastName,
                profile.Email,
                profile.PhoneNumber
            }
        });
    }

    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<IActionResult> Login(LoginRequest request)
    {
        var email = request.Email.Trim();

        var user = await _userManager.FindByEmailAsync(email);

        if (user is null)
        {
            return Unauthorized(new
            {
                message = "Invalid email or password."
            });
        }

        var profile = await _db.CustomerProfiles
            .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

        if (profile is null || !profile.IsActive)
        {
            return Unauthorized(new
            {
                message = "Invalid email or password."
            });
        }

        var result = await _signInManager.PasswordSignInAsync(
            user,
            request.Password,
            isPersistent: false,
            lockoutOnFailure: true);

        if (!result.Succeeded)
        {
            return Unauthorized(new
            {
                message = "Invalid email or password."
            });
        }

        return Ok(new
        {
            message = "Login successful.",
            customer = new
            {
                profile.CustomerId,
                profile.FirstName,
                profile.LastName,
                profile.Email
            }
        });
    }

    [HttpPost("logout")]
    [Authorize]
    public async Task<IActionResult> Logout()
    {
        await _signInManager.SignOutAsync();

        return Ok(new { message = "Logged out successfully." });
    }

    [HttpGet("me")]
    [Authorize]
    public async Task<IActionResult> Me()
    {
        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized();
        }

        var profile = await _db.CustomerProfiles
            .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

        if (profile is null || !profile.IsActive)
        {
            await _signInManager.SignOutAsync();
            return Unauthorized();
        }

        var profilePictureUrl = string.IsNullOrWhiteSpace(profile.ProfilePictureUrl)
            ? null
            : $"{Request.Scheme}://{Request.Host}{profile.ProfilePictureUrl}";

        return Ok(new
        {
            customer = new
            {
                profile.CustomerId,
                profile.FirstName,
                profile.LastName,
                profile.Email,
                profile.PhoneNumber,
                ProfilePictureUrl = profilePictureUrl
            }
        });
    }

    [HttpPost("profile-picture")]
    [Authorize]
    [Consumes("multipart/form-data")]
    public async Task<IActionResult> UploadProfilePicture(
    [FromForm] UploadProfilePictureRequest request)
    {
        var file = request.File;

        if (file is null || file.Length == 0)
        {
            return BadRequest(new
            {
                message = "Please select an image to upload."
            });
        }

        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized();
        }

        var profile = await _db.CustomerProfiles
            .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

        if (profile is null || !profile.IsActive)
        {
            return Unauthorized();
        }

        var oldPictureUrl = profile.ProfilePictureUrl;

        try
        {
            var newPictureUrl =
                await _profilePictureService.SaveAsync(file);

            profile.ProfilePictureUrl = newPictureUrl;

            await _db.SaveChangesAsync();

            _profilePictureService.Delete(oldPictureUrl);

            var pictureUrl = $"{Request.Scheme}://{Request.Host}{newPictureUrl}";

            return Ok(new
            {
                message = "Profile picture updated successfully.",
                profilePictureUrl = pictureUrl
            });
        }
        catch (InvalidOperationException exception)
        {
            return BadRequest(new
            {
                message = exception.Message
            });
        }
    }
    [HttpDelete("profile-picture")]
    [Authorize]
    public async Task<IActionResult> RemoveProfilePicture()
    {
        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized();
        }

        var profile = await _db.CustomerProfiles
            .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

        if (profile is null || !profile.IsActive)
        {
            return Unauthorized();
        }

        var oldPictureUrl = profile.ProfilePictureUrl;

        profile.ProfilePictureUrl = null;

        await _db.SaveChangesAsync();

        _profilePictureService.Delete(oldPictureUrl);

        return Ok(new
        {
            message = "Profile picture removed successfully."
        });
    }
}

public class UploadProfilePictureRequest
{
    public IFormFile? File { get; set; }
}

