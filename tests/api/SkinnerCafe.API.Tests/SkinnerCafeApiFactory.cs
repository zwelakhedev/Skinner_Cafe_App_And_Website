using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.TestHost;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;
using Microsoft.Extensions.Hosting;
using SkinnerCafe.API.Data;
using Microsoft.AspNetCore.Identity;
using SkinnerCafe.API.Services.Notifications;
using SkinnerCafe.API.Models;
using SkinnerCafe.API.Services;

namespace SkinnerCafe.API.Tests;

public sealed class SkinnerCafeApiFactory : WebApplicationFactory<Program>
{
    private readonly SqliteConnection _connection;

    public SkinnerCafeApiFactory()
    {
        _connection = new SqliteConnection("Data Source=:memory:");
        _connection.Open();
    }

    protected override void ConfigureWebHost(IWebHostBuilder builder)
{
    builder.UseEnvironment("Testing");

    builder.ConfigureServices(services =>
    {
        services
            .AddAuthentication(options =>
            {
                options.DefaultAuthenticateScheme = "TestOrCookie";
            })
            .AddPolicyScheme(
                "TestOrCookie",
                null,
                options =>
                {
                    options.ForwardDefaultSelector = context =>
                        context.Request.Headers.ContainsKey("X-Test-Role")
                            ? TestAuthenticationHandler.SchemeName
                            : IdentityConstants.ApplicationScheme;
                })
            .AddScheme<AuthenticationSchemeOptions, TestAuthenticationHandler>(
                TestAuthenticationHandler.SchemeName,
                options => { });

        services.RemoveAll<DbContextOptions<SkinnerCafeDbContext>>();
        services.RemoveAll<SkinnerCafeDbContext>();

        services.AddDbContext<SkinnerCafeDbContext>(options =>
            options.UseSqlite(_connection));

        services.RemoveAll<IOrderNotificationService>();
        services.AddSingleton<TestOrderNotificationService>();
        services.AddSingleton<IOrderNotificationService>(provider =>
            provider.GetRequiredService<TestOrderNotificationService>());
        services.RemoveAll<IGoogleIdTokenValidator>();
        services.AddScoped<IGoogleIdTokenValidator, TestGoogleIdTokenValidator>();
    });
}

    protected override IHost CreateHost(IHostBuilder builder)
    {
        var host = base.CreateHost(builder);

        // Register SQL Server's UTC timestamp function for SQLite tests.
        _connection.CreateFunction(
            "sysutcdatetime",
            () => DateTime.UtcNow.ToString("yyyy-MM-dd HH:mm:ss.fffffff"));

        using var scope = host.Services.CreateScope();

        var dbContext = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        dbContext.Database.EnsureCreated();

                dbContext.Users.AddRange(
            new ApplicationUser
            {
                Id = "test-staff-id",
                UserName = "test-staff",
                NormalizedUserName = "TEST-STAFF",
                SecurityStamp = Guid.NewGuid().ToString()
            },
            new ApplicationUser
            {
                Id = "test-admin-id",
                UserName = "test-admin",
                NormalizedUserName = "TEST-ADMIN",
                SecurityStamp = Guid.NewGuid().ToString()
            }
        );

        dbContext.SaveChanges();

        return host;
    }

    protected override void Dispose(bool disposing)
    {
        base.Dispose(disposing);

        if (disposing)
        {
            _connection.Dispose();
        }
    }
}
