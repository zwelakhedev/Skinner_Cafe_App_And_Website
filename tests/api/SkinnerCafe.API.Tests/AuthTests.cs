using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.DependencyInjection;
using SkinnerCafe.API.Models;
using Xunit;

namespace SkinnerCafe.API.Tests;

public sealed class AuthTests : IClassFixture<SkinnerCafeApiFactory>
{
    private readonly SkinnerCafeApiFactory _factory;

    public AuthTests(SkinnerCafeApiFactory factory)
    {
        _factory = factory;
    }

    private HttpClient CreateClient()
    {
        return _factory.CreateClient(
            new Microsoft.AspNetCore.Mvc.Testing.WebApplicationFactoryClientOptions
            {
                BaseAddress = new Uri("https://localhost"),
                AllowAutoRedirect = false,
                HandleCookies = true
            });
    }

    private static object CreateRegistrationRequest(string email) => new
    {
        firstName = "Test",
        lastName = "Customer",
        email,
        password = "TestPassword123!",
        phoneNumber = "0821234567"
    };

    [Fact]
    public async Task Me_WithoutAuthentication_ReturnsUnauthorized()
    {
        using var client = CreateClient();

        var response = await client.GetAsync("/api/auth/me");

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task Register_WithValidDetails_CreatesCustomer()
    {
        using var client = CreateClient();

        var email = $"customer.{Guid.NewGuid():N}@example.com";

        var response = await client.PostAsJsonAsync(
            "/api/auth/register",
            CreateRegistrationRequest(email));

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
    }

    [Fact]
    public async Task Register_WithDuplicateEmail_ReturnsConflict()
    {
        using var client = CreateClient();

        var email = $"duplicate.{Guid.NewGuid():N}@example.com";
        var request = CreateRegistrationRequest(email);

        var firstResponse = await client.PostAsJsonAsync(
            "/api/auth/register", request);

        var secondResponse = await client.PostAsJsonAsync(
            "/api/auth/register", request);

        Assert.Equal(HttpStatusCode.Created, firstResponse.StatusCode);
        Assert.Equal(HttpStatusCode.Conflict, secondResponse.StatusCode);
    }

    [Fact]
    public async Task Login_WithInvalidCredentials_ReturnsUnauthorized()
    {
        using var client = CreateClient();

        var request = new
        {
            email = $"missing.{Guid.NewGuid():N}@example.com",
            password = "WrongPassword123!"
        };

        var response = await client.PostAsJsonAsync(
            "/api/auth/login", request);

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task Login_WithValidCredentials_ReturnsSuccess()
    {
        using var client = CreateClient();

        var email = $"login.{Guid.NewGuid():N}@example.com";

        var registrationResponse = await client.PostAsJsonAsync(
            "/api/auth/register",
            CreateRegistrationRequest(email));

        var registrationBody =
            await registrationResponse.Content.ReadAsStringAsync();

        Assert.True(
            registrationResponse.StatusCode == HttpStatusCode.Created,
            $"Registration failed: {(int)registrationResponse.StatusCode} " +
            $"{registrationBody}");

        var loginResponse = await client.PostAsJsonAsync(
            "/api/auth/login",
            new
            {
                email,
                password = "TestPassword123!"
            });

        var loginBody = await loginResponse.Content.ReadAsStringAsync();

        Assert.True(
            loginResponse.StatusCode == HttpStatusCode.OK,
            $"Login failed: {(int)loginResponse.StatusCode} {loginBody}");
    }

    [Fact]
    public async Task Me_AfterSuccessfulLogin_ReturnsCustomerProfile()
    {
        using var client = CreateClient();

        var email = $"profile.{Guid.NewGuid():N}@example.com";

        var registrationResponse = await client.PostAsJsonAsync(
            "/api/auth/register",
            CreateRegistrationRequest(email));

        Assert.Equal(HttpStatusCode.Created, registrationResponse.StatusCode);

        var loginResponse = await client.PostAsJsonAsync(
            "/api/auth/login",
            new
            {
                email,
                password = "TestPassword123!"
            });

        var loginBody = await loginResponse.Content.ReadAsStringAsync();
        var setCookieHeaders = loginResponse.Headers.TryGetValues(
            "Set-Cookie", out var values)
            ? string.Join(" | ", values.Select(v => v.Split(';')[0]))
            : "(no Set-Cookie header)";

        var cookieContainer = _factory.Server.CreateHandler();

        Assert.True(
            loginResponse.StatusCode == HttpStatusCode.OK,
            $"Login failed: {(int)loginResponse.StatusCode}; body: {loginBody}");

        Assert.True(
            loginResponse.Headers.Contains("Set-Cookie"),
            $"Login succeeded but no Set-Cookie header was returned. Body: {loginBody}");

        var profileResponse = await client.GetAsync("/api/auth/me");
        var profileBody = await profileResponse.Content.ReadAsStringAsync();

        Assert.True(
            profileResponse.StatusCode == HttpStatusCode.OK,
            $"Profile failed: {(int)profileResponse.StatusCode}; " +
            $"cookie: {setCookieHeaders}; body: {profileBody}");
    }

    [Fact]
    public async Task Logout_AfterSuccessfulLogin_ReturnsSuccess()
    {
        using var client = CreateClient();

        var email = $"logout.{Guid.NewGuid():N}@example.com";

        var registrationResponse = await client.PostAsJsonAsync(
            "/api/auth/register",
            CreateRegistrationRequest(email));

        Assert.Equal(
            HttpStatusCode.Created,
            registrationResponse.StatusCode);

        var loginResponse = await client.PostAsJsonAsync(
            "/api/auth/login",
            new
            {
                email,
                password = "TestPassword123!"
            });

        Assert.Equal(HttpStatusCode.OK, loginResponse.StatusCode);

        var logoutResponse = await client.PostAsync(
            "/api/auth/logout", null);

        Assert.Equal(HttpStatusCode.OK, logoutResponse.StatusCode);

        var profileResponse = await client.GetAsync("/api/auth/me");

        Assert.Equal(
            HttpStatusCode.Unauthorized,
            profileResponse.StatusCode);
    }

    [Fact]
    public async Task Logout_WithoutAuthentication_ReturnsUnauthorized()
    {
        using var client = CreateClient();

        var response = await client.PostAsync(
            "/api/auth/logout", null);

        Assert.Equal(
            HttpStatusCode.Unauthorized,
            response.StatusCode);
    }
    private async Task<string> CreateStaffAccountAsync(string role)
{
    var email = $"{role.ToLowerInvariant()}.{Guid.NewGuid():N}@example.com";

    using var scope = _factory.Services.CreateScope();

    var userManager = scope.ServiceProvider
        .GetRequiredService<UserManager<ApplicationUser>>();

    var roleManager = scope.ServiceProvider
    .GetRequiredService<RoleManager<IdentityRole>>();

if (!await roleManager.RoleExistsAsync(role))
{
    var roleCreation = await roleManager.CreateAsync(
        new IdentityRole(role));

    Assert.True(
        roleCreation.Succeeded,
        string.Join(", ", roleCreation.Errors.Select(e => e.Description)));
}

    var user = new ApplicationUser
    {
        UserName = email,
        Email = email,
        EmailConfirmed = true
    };

    var createResult = await userManager.CreateAsync(
        user,
        "TestPassword123!");

    Assert.True(
        createResult.Succeeded,
        string.Join(", ", createResult.Errors.Select(e => e.Description)));

    var roleResult = await userManager.AddToRoleAsync(user, role);

    Assert.True(
        roleResult.Succeeded,
        string.Join(", ", roleResult.Errors.Select(e => e.Description)));

    return email;
}

[Theory]
[InlineData("Staff")]
[InlineData("Admin")]
public async Task StaffLogin_WithValidCredentials_ReturnsSuccess(string role)
{
    using var client = CreateClient();

    var email = await CreateStaffAccountAsync(role);

    var response = await client.PostAsJsonAsync(
        "/api/auth/staff/login",
        new
        {
            email,
            password = "TestPassword123!"
        });

    var body = await response.Content.ReadAsStringAsync();

    Assert.True(
        response.StatusCode == HttpStatusCode.OK,
        $"Staff login failed: {(int)response.StatusCode} {body}");

    Assert.Contains(email, body);
}

[Fact]
public async Task StaffLogin_CustomerAccount_ReturnsUnauthorized()
{
    using var client = CreateClient();

    var email = $"customer.staff.{Guid.NewGuid():N}@example.com";

    var registrationResponse = await client.PostAsJsonAsync(
        "/api/auth/register",
        CreateRegistrationRequest(email));

    Assert.Equal(HttpStatusCode.Created, registrationResponse.StatusCode);

    var response = await client.PostAsJsonAsync(
        "/api/auth/staff/login",
        new
        {
            email,
            password = "TestPassword123!"
        });

    Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
}

[Fact]
public async Task StaffMe_AfterSuccessfulLogin_ReturnsStaffProfile()
{
    using var client = CreateClient();

    var email = await CreateStaffAccountAsync("Staff");

    var loginResponse = await client.PostAsJsonAsync(
        "/api/auth/staff/login",
        new
        {
            email,
            password = "TestPassword123!"
        });

    Assert.Equal(HttpStatusCode.OK, loginResponse.StatusCode);

    var profileResponse = await client.GetAsync("/api/auth/staff/me");
    var body = await profileResponse.Content.ReadAsStringAsync();

    Assert.True(
        profileResponse.StatusCode == HttpStatusCode.OK,
        $"Staff profile failed: {(int)profileResponse.StatusCode} {body}");

    Assert.Contains(email, body);
    Assert.Contains("Staff", body);
}

[Fact]
public async Task StaffLogout_AfterSuccessfulLogin_InvalidatesSession()
{
    using var client = CreateClient();

    var email = await CreateStaffAccountAsync("Staff");

    var loginResponse = await client.PostAsJsonAsync(
        "/api/auth/staff/login",
        new
        {
            email,
            password = "TestPassword123!"
        });

    Assert.Equal(HttpStatusCode.OK, loginResponse.StatusCode);

    var logoutResponse = await client.PostAsync(
        "/api/auth/staff/logout", null);

    Assert.Equal(HttpStatusCode.OK, logoutResponse.StatusCode);

    var profileResponse = await client.GetAsync("/api/auth/staff/me");

    Assert.Equal(
        HttpStatusCode.Unauthorized,
        profileResponse.StatusCode);
}

}

