using System.Net;
using System.Net.Http.Json;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.Models;

namespace SkinnerCafe.API.Tests;

public sealed class MenuTests : IClassFixture<SkinnerCafeApiFactory>
{
    private readonly SkinnerCafeApiFactory _factory;

    public MenuTests(SkinnerCafeApiFactory factory)
    {
        _factory = factory;
    }

    [Fact]
    public async Task GetCategories_ReturnsCategoriesInAlphabeticalOrder()
    {
        using var client = _factory.CreateClient();

        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider
                .GetRequiredService<SkinnerCafeDbContext>();

            db.Categories.AddRange(
                new Category
                {
                    CategoryName = "Tea",
                    DisplayOrder = 2,
                    IsActive = true
                },
                new Category
                {
                    CategoryName = "Bakery",
                    DisplayOrder = 1,
                    IsActive = true
                });

            await db.SaveChangesAsync();
        }

        var response = await client.GetAsync("/api/menu/categories");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);

        var categories = await response.Content
            .ReadFromJsonAsync<List<CategoryResponse>>();

        Assert.NotNull(categories);

        Assert.Equal(
            new[] { "Bakery", "Tea" },
            categories.Select(category => category.CategoryName).ToArray());
    }

    [Fact]
    public async Task GetCategories_WhenCatalogueIsEmpty_ReturnsEmptyList()
    {
        using var client = _factory.CreateClient();

        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider
                .GetRequiredService<SkinnerCafeDbContext>();

            db.Categories.RemoveRange(await db.Categories.ToListAsync());
            await db.SaveChangesAsync();
        }

        var response = await client.GetAsync("/api/menu/categories");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);

        var categories = await response.Content
            .ReadFromJsonAsync<List<CategoryResponse>>();

        Assert.NotNull(categories);
        Assert.Empty(categories);
    }

    [Fact]
    public async Task GetProducts_ReturnsOnlyActiveProductsAndActiveVariants()
    {
        using var client = _factory.CreateClient();

        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider
                .GetRequiredService<SkinnerCafeDbContext>();

            var category = new Category
            {
                CategoryName = "Coffee",
                DisplayOrder = 1,
                IsActive = true
            };

            var activeProduct = new Product
            {
                ProductName = "Latte",
                Description = "Espresso with milk",
                IsActive = true,
                IsStockTracked = false,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow,
                Category = category
            };

            activeProduct.ProductVariants.Add(
                new ProductVariant
                {
                    VariantName = "Regular",
                    Price = 30m,
                    IsActive = true,
                    DisplayOrder = 1
                });

            activeProduct.ProductVariants.Add(
                new ProductVariant
                {
                    VariantName = "Large",
                    Price = 38m,
                    IsActive = false,
                    DisplayOrder = 2
                });

            var inactiveProduct = new Product
            {
                ProductName = "Discontinued Coffee",
                IsActive = false,
                IsStockTracked = false,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow,
                Category = category
            };

            db.Products.AddRange(activeProduct, inactiveProduct);
            await db.SaveChangesAsync();
        }

        var response = await client.GetAsync("/api/menu/products");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);

        var products = await response.Content
            .ReadFromJsonAsync<List<ProductResponse>>();

        Assert.NotNull(products);

        var product = Assert.Single(products);
        Assert.Equal("Latte", product.ProductName);
        Assert.Equal("Coffee", product.Category);

        var variant = Assert.Single(product.Variants);
        Assert.Equal("Regular", variant.VariantName);
        Assert.Equal(30m, variant.Price);
    }

    private sealed record CategoryResponse(
        int CategoryId,
        string CategoryName);

    private sealed record ProductResponse(
        int ProductId,
        string ProductName,
        string? Description,
        string Category,
        List<VariantResponse> Variants);

    private sealed record VariantResponse(
        int VariantId,
        string VariantName,
        decimal Price);
}
