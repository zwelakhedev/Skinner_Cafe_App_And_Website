using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;
using Microsoft.Extensions.Hosting;
using SkinnerCafe.API.Data;

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
            services.RemoveAll<DbContextOptions<SkinnerCafeDbContext>>();
            services.RemoveAll<SkinnerCafeDbContext>();

            services.AddDbContext<SkinnerCafeDbContext>(options =>
                options.UseSqlite(_connection));
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
