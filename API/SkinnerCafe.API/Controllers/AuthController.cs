using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.DTOs;
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
    private readonly IGoogleIdTokenValidator _googleValidator;

    public AuthController(
        UserManager<ApplicationUser> userManager,
        SignInManager<ApplicationUser> signInManager,
        SkinnerCafeDbContext db,
        IGoogleIdTokenValidator googleValidator,
        ProfilePictureService profilePictureService)
    {
        _userManager = userManager;
        _signInManager = signInManager;
        _db = db;
        _profilePictureService = profilePictureService;
        _googleValidator = googleValidator;
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

    public record GoogleLoginRequest(
    [Required] string IdToken);

    public record SetPasswordRequest(
    [Required] string IdToken,
    [Required, StringLength(128, MinimumLength = 8)] string NewPassword);

    public record ChangePasswordRequest(
    [Required] string CurrentPassword,
    [Required, StringLength(128, MinimumLength = 8)] string NewPassword);

    public record UnlinkGoogleAccountRequest(
    [Required] string CurrentPassword);

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
        await _signInManager.SignInAsync(user, isPersistent: true);

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
            isPersistent: true,
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

    [HttpPost("google")]
    [AllowAnonymous]
    public async Task<IActionResult> GoogleLogin(GoogleLoginRequest request)
    {
        var google = await _googleValidator.ValidateAsync(request.IdToken);

        if (google is null)
            return Unauthorized(new { message = "Invalid Google token." });

        var user = await _userManager.FindByLoginAsync(
            "Google", google.Subject);

        if (user is not null)
        {
            var roles = await _userManager.GetRolesAsync(user);

            if (roles.Any(r => r is "Staff" or "Admin"))
                return Unauthorized();

            var matchingCustomerProfile = await _db.CustomerProfiles
                .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

            if (matchingCustomerProfile is null || !matchingCustomerProfile.IsActive)
                return Unauthorized();

            await _signInManager.SignInAsync(user, true);

            return Ok(new
            {
                message = "Google login successful.",
                customer = new
                {
                    matchingCustomerProfile.CustomerId,
                    matchingCustomerProfile.FirstName,
                    matchingCustomerProfile.LastName,
                    matchingCustomerProfile.Email
                }
            });
        }

        var existingUser = await _userManager.FindByEmailAsync(google.Email);

        var existingProfile = await _db.CustomerProfiles
            .AnyAsync(c => c.Email == google.Email);

        if (existingUser is not null || existingProfile)
        {
            return Conflict(new
            {
                message = "Please sign in with your password before linking Google."
            });
        }

        return await CreateGoogleCustomerAsync(google);
    }


    [HttpPost("google/link")]
    [Authorize]
    public async Task<IActionResult> LinkGoogleAccount(
        GoogleLoginRequest request)
    {
        var google = await _googleValidator.ValidateAsync(request.IdToken);

        if (google is null)
        {
            return BadRequest(new
            {
                message = "Invalid Google token."
            });
        }

        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized(new
            {
                message = "Please sign in before linking Google."
            });
        }

        var roles = await _userManager.GetRolesAsync(user);

        if (roles.Any(r => r is "Staff" or "Admin"))
        {
            return Forbid();
        }

        var profile = await _db.CustomerProfiles
            .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

        if (profile is null || !profile.IsActive)
        {
            return Unauthorized(new
            {
                message = "An active customer account is required."
            });
        }

        if (!string.Equals(
            user.Email,
            google.Email,
            StringComparison.OrdinalIgnoreCase))
        {
            return Conflict(new
            {
                message = "Your Google email must match your customer account email."
            });
        }

        var existingGoogleUser = await _userManager.FindByLoginAsync(
            "Google",
            google.Subject);

        if (existingGoogleUser is not null)
        {
            if (existingGoogleUser.Id == user.Id)
            {
                return Ok(new
                {
                    message = "This Google account is already linked."
                });
            }

            return Conflict(new
            {
                message = "This Google account is already linked to another account."
            });
        }

        var existingLogins = await _userManager.GetLoginsAsync(user);

        if (existingLogins.Any(login => login.LoginProvider == "Google"))
        {
            return Conflict(new
            {
                message = "A Google account is already linked. Unlink it before linking another Google account."
            });
        }

        var result = await _userManager.AddLoginAsync(
            user,
            new UserLoginInfo("Google", google.Subject, "Google"));

        if (!result.Succeeded)
        {
            return Conflict(new
            {
                message = "Google account could not be linked.",
                errors = result.Errors.Select(e => e.Description)
            });
        }

        await _signInManager.RefreshSignInAsync(user);

        return Ok(new
        {
            message = "Google account linked successfully."
        });
    }

    [HttpDelete("google/unlink")]
    [Authorize]
    public async Task<IActionResult> UnlinkGoogleAccount(
    [FromBody] UnlinkGoogleAccountRequest request)
    {
        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized(new
            {
                message = "Please sign in before unlinking Google."
            });
        }

        var roles = await _userManager.GetRolesAsync(user);

        if (roles.Any(r => r is "Staff" or "Admin"))
        {
            return Forbid();
        }

        var profile = await _db.CustomerProfiles
            .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

        if (profile is null || !profile.IsActive)
        {
            return Unauthorized(new
            {
                message = "An active customer account is required."
            });
        }

        if (!await _userManager.HasPasswordAsync(user))
        {
            return Conflict(new
            {
                message = "Create a password before unlinking Google so you do not lose access to your account."
            });
        }

        if (!await _userManager.CheckPasswordAsync(user, request.CurrentPassword))
        {
            return BadRequest(new
            {
                message = "The current password is incorrect."
            });
        }

        var googleLogin = (await _userManager.GetLoginsAsync(user))
            .FirstOrDefault(login => login.LoginProvider == "Google");

        if (googleLogin is null)
        {
            return Conflict(new
            {
                message = "No Google account is currently linked."
            });
        }

        var result = await _userManager.RemoveLoginAsync(
            user,
            googleLogin.LoginProvider,
            googleLogin.ProviderKey);

        if (!result.Succeeded)
        {
            return Conflict(new
            {
                message = "Google account could not be unlinked.",
                errors = result.Errors.Select(e => e.Description)
            });
        }

        await _signInManager.RefreshSignInAsync(user);

        return Ok(new
        {
            message = "Google account unlinked successfully."
        });
    }

    [HttpPost("password/set")]
    [Authorize]
    public async Task<IActionResult> SetPassword(
    SetPasswordRequest request)
    {
        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized(new
            {
                message = "Please sign in before setting a password."
            });
        }

        var roles = await _userManager.GetRolesAsync(user);

        if (roles.Any(r => r is "Staff" or "Admin"))
        {
            return Forbid();
        }

        var profile = await _db.CustomerProfiles
            .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

        if (profile is null || !profile.IsActive)
        {
            return Unauthorized(new
            {
                message = "An active customer account is required."
            });
        }

        if (await _userManager.HasPasswordAsync(user))
        {
            return Conflict(new
            {
                message = "Your account already has a password. Use the change password feature instead."
            });
        }

        var google = await _googleValidator.ValidateAsync(request.IdToken);

        if (google is null)
        {
            return BadRequest(new
            {
                message = "Invalid Google token."
            });
        }

        var linkedGoogleUser = await _userManager.FindByLoginAsync(
            "Google",
            google.Subject);

        if (linkedGoogleUser is null || linkedGoogleUser.Id != user.Id)
        {
            return Conflict(new
            {
                message = "Please verify the Google account already linked to your customer account."
            });
        }

        if (!string.Equals(
            user.Email,
            google.Email,
            StringComparison.OrdinalIgnoreCase))
        {
            return Conflict(new
            {
                message = "Your Google email does not match your customer account."
            });
        }

        var result = await _userManager.AddPasswordAsync(
            user,
            request.NewPassword);

        if (!result.Succeeded)
        {
            return BadRequest(new
            {
                message = "The password does not meet the account requirements.",
                errors = result.Errors.Select(e => e.Description)
            });
        }

        await _signInManager.RefreshSignInAsync(user);

        return Ok(new
        {
            message = "Password created successfully."
        });
    }

    [HttpPost("password/change")]
    [Authorize]
    public async Task<IActionResult> ChangePassword(
    ChangePasswordRequest request)
    {
        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized(new
            {
                message = "Please sign in before changing your password."
            });
        }

        var roles = await _userManager.GetRolesAsync(user);

        if (roles.Any(r => r is "Staff" or "Admin"))
        {
            return Forbid();
        }

        var profile = await _db.CustomerProfiles
            .SingleOrDefaultAsync(c => c.IdentityUserId == user.Id);

        if (profile is null || !profile.IsActive)
        {
            return Unauthorized(new
            {
                message = "An active customer account is required."
            });
        }

        if (!await _userManager.HasPasswordAsync(user))
        {
            return Conflict(new
            {
                message = "Your account does not have a password yet. Please create one first."
            });
        }

        var result = await _userManager.ChangePasswordAsync(
            user,
            request.CurrentPassword,
            request.NewPassword);

        if (!result.Succeeded)
        {
            return BadRequest(new
            {
                message = "The password could not be changed.",
                errors = result.Errors.Select(e => e.Description)
            });
        }

        await _signInManager.RefreshSignInAsync(user);

        return Ok(new
        {
            message = "Password changed successfully."
        });
    }

    private async Task<IActionResult> CreateGoogleCustomerAsync(
    GoogleUserInfo google)
    {
        await using var transaction =
            await _db.Database.BeginTransactionAsync();

        var user = new ApplicationUser
        {
            UserName = google.Email,
            Email = google.Email,
            EmailConfirmed = true
        };

        var result = await _userManager.CreateAsync(user);

        if (!result.Succeeded)
        {
            await transaction.RollbackAsync();
            return BadRequest(new { message = "Could not create account." });
        }

        var profile = new CustomerProfile
        {
            IdentityUserId = user.Id,
            FirstName = string.IsNullOrWhiteSpace(google.FirstName)
                ? google.Email.Split('@')[0]
                : google.FirstName,
            LastName = string.IsNullOrWhiteSpace(google.LastName)
                ? "Customer"
                : google.LastName,
            Email = google.Email,
            IsActive = true
        };

        _db.CustomerProfiles.Add(profile);

        var loginResult = await _userManager.AddLoginAsync(
            user,
            new UserLoginInfo("Google", google.Subject, "Google"));

        if (!loginResult.Succeeded)
        {
            await transaction.RollbackAsync();
            return Conflict(new { message = "Google account could not be linked." });
        }

        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        await _signInManager.SignInAsync(user, isPersistent: true);

        return Ok(new
        {
            message = "Google account created successfully.",
            customer = new
            {
                profile.CustomerId,
                profile.FirstName,
                profile.LastName,
                profile.Email
            }
        });
    }

    [HttpPost("staff/login")]
    [AllowAnonymous]
    public async Task<IActionResult> StaffLogin(LoginRequest request)
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

        var roles = await _userManager.GetRolesAsync(user);

        if (!roles.Any(role =>
                role is "Staff" or "Admin"))
        {
            return Unauthorized(new
            {
                message = "Invalid email or password."
            });
        }

        var result = await _signInManager.PasswordSignInAsync(
            user,
            request.Password,
            isPersistent: true,
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
            message = "Staff login successful.",
            staff = new
            {
                user.Id,
                user.Email,
                Roles = roles
            }
        });
    }

    [HttpGet("staff/me")]
    [Authorize(Roles = "Staff,Admin")]
    public async Task<IActionResult> StaffMe()
    {
        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized();
        }

        var roles = await _userManager.GetRolesAsync(user);

        return Ok(new
        {
            staff = new
            {
                user.Id,
                user.Email,
                Roles = roles
            }
        });
    }

    [HttpPost("staff/logout")]
    [Authorize(Roles = "Staff,Admin")]
    public async Task<IActionResult> StaffLogout()
    {
        await _signInManager.SignOutAsync();

        return Ok(new
        {
            message = "Staff logged out successfully."
        });
    }

    [HttpPost("device-token")]
    [Authorize]
    public async Task<IActionResult> RegisterDeviceToken(
        RegisterDeviceTokenRequest request)
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

        var token = request.Token.Trim();

        var deviceToken = await _db.DeviceTokens
            .SingleOrDefaultAsync(d => d.Token == token);

        if (deviceToken is null)
        {
            deviceToken = new DeviceToken
            {
                IdentityUserId = user.Id,
                Token = token,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow,
                IsActive = true
            };

            _db.DeviceTokens.Add(deviceToken);
        }
        else
        {
            // A device token can belong to only one account at a time.
            deviceToken.IdentityUserId = user.Id;
            deviceToken.UpdatedAt = DateTime.UtcNow;
            deviceToken.IsActive = true;
        }

        await _db.SaveChangesAsync();

        return Ok(new
        {
            message = "Device token registered successfully."
        });
    }

    [HttpPost("device-token/unregister")]
    [Authorize]
    public async Task<IActionResult> UnregisterDeviceToken(
        RegisterDeviceTokenRequest request)
    {
        var user = await _userManager.GetUserAsync(User);

        if (user is null)
        {
            return Unauthorized();
        }

        var token = request.Token.Trim();

        var deviceToken = await _db.DeviceTokens
            .SingleOrDefaultAsync(d =>
                d.Token == token &&
                d.IdentityUserId == user.Id);

        if (deviceToken is not null)
        {
            deviceToken.IsActive = false;
            deviceToken.UpdatedAt = DateTime.UtcNow;

            await _db.SaveChangesAsync();
        }

        return Ok(new
        {
            message = "Device token unregistered successfully."
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

        var hasPassword = await _userManager.HasPasswordAsync(user);

        var externalLogins = await _userManager.GetLoginsAsync(user);

        var isGoogleLinked = externalLogins.Any(
            login => login.LoginProvider == "Google"
        );

        return Ok(new
        {
            hasPassword,
            isGoogleLinked,

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

