using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;

namespace SkinnerCafe.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class MenuController : ControllerBase
{
    private readonly SkinnerCafeDbContext _context;

    public MenuController(SkinnerCafeDbContext context)
    {
        _context = context;
    }

    [HttpGet("categories")]
    public async Task<IActionResult> GetCategories()
    {
        var categories = await _context.Categories
            .AsNoTracking()
            .OrderBy(category => category.CategoryName)
            .Select(category => new
            {
                category.CategoryId,
                category.CategoryName
            })
            .ToListAsync();

        return Ok(categories);
    }

    [HttpGet("products")]
    public async Task<IActionResult> GetProducts()
    {
        var products = await _context.Products
            .AsNoTracking()
            .Where(product => product.IsActive)
            .OrderBy(product => product.Category.CategoryName)
            .ThenBy(product => product.ProductName)
            .Select(product => new
            {
                product.ProductId,
                product.ProductName,
                product.Description,
                Category = product.Category.CategoryName,

                Variants = product.ProductVariants
                    .Where(variant => variant.IsActive)
                    .OrderBy(variant => variant.DisplayOrder)
                    .Select(variant => new
                    {
                        variant.VariantId,
                        variant.VariantName,
                        variant.Price
                    })
                    .ToList(),

                AddOns = product.AddOns
                    .Where(addOn => addOn.IsActive)
                    .OrderBy(addOn => addOn.AddOnName)
                    .Select(addOn => new
                    {
                        addOn.AddOnId,
                        addOn.AddOnName,
                        addOn.Price
                    })
                    .ToList()
            })
            .ToListAsync();

        return Ok(products);
    }
}
