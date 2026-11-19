using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.Models;
using SkinnerCafe.API.Services;
using SkinnerCafe.API.Services.Notifications;
using System.Security.Cryptography.X509Certificates;
using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;

var builder = WebApplication.CreateBuilder(args);

// Initialize Firebase Admin SDK outside automated tests.
if (!builder.Environment.IsEnvironment("Testing"))
{
    var serviceAccountPath =
        builder.Configuration["Firebase:ServiceAccountPath"];

    if (
        string.IsNullOrWhiteSpace(serviceAccountPath) ||
        !File.Exists(serviceAccountPath)
    )
    {
        throw new InvalidOperationException(
            "Firebase service account credentials are missing or invalid.");
    }

    FirebaseApp.Create(new AppOptions
    {
        Credential = CredentialFactory
            .FromFile<ServiceAccountCredential>(serviceAccountPath)
            .ToGoogleCredential()
    });
}

// Configure the mkcert HTTPS certificate for local development only.
if (builder.Environment.IsDevelopment())
{
    var certificatePath = Path.GetFullPath(
        Path.Combine(
            builder.Environment.ContentRootPath,
            "..",
            "certs",
            "skinnercafe-dev.pfx"));

    var certificatePassword =
        builder.Configuration["Certificates:Password"];

    if (string.IsNullOrWhiteSpace(certificatePassword))
    {
        throw new InvalidOperationException(
            "The development certificate password is missing from User Secrets.");
    }

    var certificate = X509CertificateLoader.LoadPkcs12FromFile(
        certificatePath,
        certificatePassword,
        X509KeyStorageFlags.UserKeySet |
        X509KeyStorageFlags.PersistKeySet);

    builder.WebHost.ConfigureKestrel(options =>
    {
        options.ListenAnyIP(5043);

        options.ListenAnyIP(7277, listenOptions =>
        {
            listenOptions.UseHttps(certificate);
        });
    });
}

// Register Data Protection for guest order token protection.
var dataProtectionBuilder = builder.Services
    .AddDataProtection()
    .SetApplicationName("SkinnerCafe");

// Persist encryption keys outside automated tests.
if (!builder.Environment.IsEnvironment("Testing"))
{
    var keyDirectory = builder.Configuration["DataProtection:KeyDirectory"];

    if (string.IsNullOrWhiteSpace(keyDirectory))
    {
        if (!builder.Environment.IsDevelopment())
        {
            throw new InvalidOperationException(
                "DataProtection:KeyDirectory must be configured outside Testing and Development.");
        }

        keyDirectory = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "SkinnerCafe",
            "DataProtection-Keys");
    }

    Directory.CreateDirectory(keyDirectory);

    dataProtectionBuilder.PersistKeysToFileSystem(
        new DirectoryInfo(keyDirectory));
}

// Register controllers.
builder.Services.AddControllers();

builder.Services.AddScoped<ProfilePictureService>();
builder.Services.AddScoped<IGoogleIdTokenValidator, GoogleIdTokenValidator>();

builder.Services.AddScoped<
    IOrderNotificationService,
    FirebaseOrderNotificationService>();

// Register SQL Server for normal application environments.
// The test factory supplies SQLite when running automated tests.
if (!builder.Environment.IsEnvironment("Testing"))
{
    builder.Services.AddDbContext<SkinnerCafeDbContext>(options =>
        options.UseSqlServer(
            builder.Configuration.GetConnectionString("SkinnerCafe")));
}

// Register ASP.NET Core Identity.
builder.Services
    .AddIdentity<ApplicationUser, IdentityRole>(options =>
    {
        // Require unique email addresses.
        options.User.RequireUniqueEmail = true;

        // Password requirements.
        options.Password.RequiredLength = 8;
        options.Password.RequireDigit = true;
        options.Password.RequireLowercase = true;
        options.Password.RequireUppercase = true;
        options.Password.RequireNonAlphanumeric = true;

        // Account lockout protection.
        options.Lockout.MaxFailedAccessAttempts = 5;
        options.Lockout.DefaultLockoutTimeSpan = TimeSpan.FromMinutes(15);
        options.Lockout.AllowedForNewUsers = true;
    })
    .AddEntityFrameworkStores<SkinnerCafeDbContext>()
    .AddDefaultTokenProviders();

// Configure authentication cookies for the API.
builder.Services.ConfigureApplicationCookie(options =>
{
    options.Cookie.Name = "SkinnerCafe.Auth";
    options.Cookie.HttpOnly = true;
    options.Cookie.SecurePolicy = CookieSecurePolicy.Always;
    options.Cookie.SameSite = SameSiteMode.Lax;

    options.SlidingExpiration = true;
    options.ExpireTimeSpan = TimeSpan.FromHours(8);

    // Return HTTP status codes for API requests instead of redirects.
    options.Events.OnRedirectToLogin = context =>
    {
        if (context.Request.Path.StartsWithSegments("/api"))
        {
            context.Response.StatusCode = StatusCodes.Status401Unauthorized;
            return Task.CompletedTask;
        }

        context.Response.Redirect(context.RedirectUri);
        return Task.CompletedTask;
    };

    options.Events.OnRedirectToAccessDenied = context =>
    {
        if (context.Request.Path.StartsWithSegments("/api"))
        {
            context.Response.StatusCode = StatusCodes.Status403Forbidden;
            return Task.CompletedTask;
        }

        context.Response.Redirect(context.RedirectUri);
        return Task.CompletedTask;
    };
});

// Allow the local React website to call the API.
builder.Services.AddCors(options =>
{
    options.AddPolicy("SkinnerCafeWeb", policy =>
    {
        policy
            .WithOrigins(
                "http://localhost:5173",
                "https://localhost:5173",
                "http://127.0.0.1:5173",
                "https://127.0.0.1:5173")
            .AllowAnyHeader()
            .AllowAnyMethod()
            .AllowCredentials();
    });
});

// Register OpenAPI.
builder.Services.AddOpenApi();

var app = builder.Build();

// Initialize application roles outside automated tests.
if (!app.Environment.IsEnvironment("Testing"))
{
    using var scope = app.Services.CreateScope();

    var roleManager =
        scope.ServiceProvider.GetRequiredService<RoleManager<IdentityRole>>();

    string[] roles = { "Staff", "Admin" };

    foreach (var role in roles)
    {
        if (!await roleManager.RoleExistsAsync(role))
        {
            var result = await roleManager.CreateAsync(
                new IdentityRole(role));

            if (!result.Succeeded)
            {
                var errors = string.Join(
                    "; ",
                    result.Errors.Select(error => error.Description));

                throw new InvalidOperationException(
                    $"Failed to create the {role} role: {errors}");
            }
        }
    }
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

app.UseCors("SkinnerCafeWeb");
app.UseStaticFiles();

app.UseAuthentication();
app.UseAuthorization();

app.MapGet("/api/health/database", async (SkinnerCafeDbContext db) =>
{
    var canConnect = await db.Database.CanConnectAsync();

    return canConnect
        ? Results.Ok(new { status = "Connected", database = "SkinnerCafe" })
        : Results.Problem("Could not connect to the SkinnerCafe database.");
});

app.MapControllers();

app.Run();

public partial class Program
{
}
