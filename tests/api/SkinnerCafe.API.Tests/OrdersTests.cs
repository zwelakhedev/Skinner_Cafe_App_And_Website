using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.Models;
using Xunit;

namespace SkinnerCafe.API.Tests;

public sealed class OrdersTests : IClassFixture<SkinnerCafeApiFactory>
{
    private readonly SkinnerCafeApiFactory _factory;

    public OrdersTests(SkinnerCafeApiFactory factory)
    {
        _factory = factory;
    }

    private async Task<int> SeedProductVariantAsync()
    {
        using var scope = _factory.Services.CreateScope();

        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var category = new Category
        {
            CategoryName = $"Test Coffee {Guid.NewGuid():N}",
            DisplayOrder = 1,
            IsActive = true
        };

        var product = new Product
        {
            ProductName = $"Test Americano {Guid.NewGuid():N}",
            Description = "Test product for order creation.",
            IsStockTracked = false,
            IsActive = true,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow,
            Category = category
        };

        var variant = new ProductVariant
        {
            VariantName = "Regular",
            Price = 32.50m,
            IsActive = true,
            DisplayOrder = 1
        };

        product.ProductVariants.Add(variant);

        db.Products.Add(product);

        await db.SaveChangesAsync();

        return variant.VariantId;
    }

    private static object CreateOrderRequest(int variantId) => new
    {
        customerName = "Test Guest",
        customerEmail = "guest@example.com",
        customerPhone = "0821234567",
        items = new[]
        {
            new
            {
                variantId,
                quantity = 2,
                addOnIds = Array.Empty<int>()
            }
        }
    };

    private static async Task<HttpResponseMessage> SendCreateOrderAsync(
        HttpClient client,
        Guid idempotencyKey,
        int variantId)
    {
        using var request = new HttpRequestMessage(
            HttpMethod.Post,
            "/api/orders");

        request.Headers.Add(
            "Idempotency-Key",
            idempotencyKey.ToString("D"));

        request.Content = JsonContent.Create(
            CreateOrderRequest(variantId));

        return await client.SendAsync(request);
    }

    [Fact]
public async Task CreateOrder_WithSameIdempotencyKey_ReturnsOriginalOrder()
{
    using var client = _factory.CreateClient();

    var variantId = await SeedProductVariantAsync();
    var idempotencyKey = Guid.NewGuid();

    var firstResponse = await SendCreateOrderAsync(
        client,
        idempotencyKey,
        variantId);

    var firstBody = await firstResponse.Content
        .ReadFromJsonAsync<JsonElement>();

    Assert.Equal(HttpStatusCode.Created, firstResponse.StatusCode);

    var firstOrderId = firstBody
        .GetProperty("order")
        .GetProperty("orderId")
        .GetInt32();

    var firstGuestToken = firstBody
        .GetProperty("guestAccessToken")
        .GetString();

    Assert.False(string.IsNullOrWhiteSpace(firstGuestToken));

    var secondResponse = await SendCreateOrderAsync(
        client,
        idempotencyKey,
        variantId);

    var secondBody = await secondResponse.Content
        .ReadFromJsonAsync<JsonElement>();

    Assert.Equal(HttpStatusCode.OK, secondResponse.StatusCode);

    var secondOrderId = secondBody
        .GetProperty("order")
        .GetProperty("orderId")
        .GetInt32();

    var secondGuestToken = secondBody
        .GetProperty("guestAccessToken")
        .GetString();

    Assert.Equal(firstOrderId, secondOrderId);
    Assert.Equal(firstGuestToken, secondGuestToken);

    using var scope = _factory.Services.CreateScope();

    var db = scope.ServiceProvider
        .GetRequiredService<SkinnerCafeDbContext>();

    var orderCount = await db.Orders.CountAsync(
        order => order.IdempotencyKey == idempotencyKey.ToString("D"));

    Assert.Equal(1, orderCount);
}

    [Fact]
    public async Task CreateOrder_WithoutIdempotencyKey_ReturnsBadRequest()
    {
        using var client = _factory.CreateClient();

        var variantId = await SeedProductVariantAsync();

        var response = await client.PostAsJsonAsync(
            "/api/orders",
            CreateOrderRequest(variantId));

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task CreateOrder_WithInvalidIdempotencyKey_ReturnsBadRequest()
    {
        using var client = _factory.CreateClient();

        var variantId = await SeedProductVariantAsync();

        using var request = new HttpRequestMessage(
            HttpMethod.Post,
            "/api/orders");

        request.Headers.Add("Idempotency-Key", "not-a-valid-guid");

        request.Content = JsonContent.Create(
            CreateOrderRequest(variantId));

        var response = await client.SendAsync(request);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task CreateOrder_WithSameKeyAndDifferentRequest_ReturnsConflict()
    {
        using var client = _factory.CreateClient();

        var variantId = await SeedProductVariantAsync();
        var idempotencyKey = Guid.NewGuid();

        var firstResponse = await SendCreateOrderAsync(
            client,
            idempotencyKey,
            variantId);

        Assert.Equal(HttpStatusCode.Created, firstResponse.StatusCode);

        var differentRequest = new
        {
            customerName = "Different Guest",
            customerEmail = "guest@example.com",
            customerPhone = "0821234567",
            items = new[]
            {
                new
                {
                    variantId,
                    quantity = 2,
                    addOnIds = Array.Empty<int>()
                }
            }
        };

        using var request = new HttpRequestMessage(
            HttpMethod.Post,
            "/api/orders");

        request.Headers.Add(
            "Idempotency-Key",
            idempotencyKey.ToString("D"));

        request.Content = JsonContent.Create(differentRequest);

        var secondResponse = await client.SendAsync(request);

        Assert.Equal(HttpStatusCode.Conflict, secondResponse.StatusCode);
    }

}