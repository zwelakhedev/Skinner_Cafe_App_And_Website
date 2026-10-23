using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;

var builder = WebApplication.CreateBuilder(args);

// Register controllers.
builder.Services.AddControllers();

// Register Entity Framework Core with the local SkinnerCafe database.
builder.Services.AddDbContext<SkinnerCafeDbContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString("SkinnerCafe")));

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
            .AllowAnyMethod();
    });
});

// Register OpenAPI.
builder.Services.AddOpenApi();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

app.UseCors("SkinnerCafeWeb");

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
