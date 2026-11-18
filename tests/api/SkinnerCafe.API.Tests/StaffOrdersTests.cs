using System.Net;
using System.Net.Http.Json;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using SkinnerCafe.API.Data;
using SkinnerCafe.API.Models;
using SkinnerCafe.API.DTOs;
using SkinnerCafe.API.Services.Notifications;
using Xunit;

namespace SkinnerCafe.API.Tests;

public sealed class StaffOrdersTests : IClassFixture<SkinnerCafeApiFactory>
{
    private readonly SkinnerCafeApiFactory _factory;

    public StaffOrdersTests(SkinnerCafeApiFactory factory)
    {
        _factory = factory;
    }

    [Fact]
    public async Task StaffCanConfirmPendingOrderAndNotifyCustomer()
    {
        var notificationService =
            _factory.Services.GetRequiredService<TestOrderNotificationService>();

        notificationService.Clear();

        int orderId;

        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider
                .GetRequiredService<SkinnerCafeDbContext>();

            db.Users.Add(new ApplicationUser
            {
                Id = "test-customer-id",
                UserName = "test-customer",
                NormalizedUserName = "TEST-CUSTOMER",
                SecurityStamp = Guid.NewGuid().ToString()
            });

            var customer = new CustomerProfile
            {
                IdentityUserId = "test-customer-id",
                FirstName = "Test",
                LastName = "Customer",
                Email = "staff-order-customer@example.com",
                CreatedAt = DateTime.UtcNow,
                IsActive = true
            };

            var order = new Order
            {
                OrderNumber = $"TEST-{Guid.NewGuid():N}",
                Customer = customer,
                OrderSource = "Online",
                OrderStatus = "Pending",
                PaymentStatus = "Pending",
                CustomerName = "Test Customer",
                CustomerEmail = customer.Email,
                CustomerPhone = "0821234567",
                Subtotal = 100m,
                DiscountAmount = 0m,
                TotalAmount = 100m,
                CreatedAt = DateTime.UtcNow
            };

            db.Orders.Add(order);
            await db.SaveChangesAsync();

            orderId = order.OrderId;
        }

        using var client = _factory.CreateClient();

        client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

        var response = await client.PutAsJsonAsync(
            $"/api/staff/orders/{orderId}/status",
            new
            {
                status = "Confirmed",
                notes = "Order approved for preparation."
            });

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);

        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider
                .GetRequiredService<SkinnerCafeDbContext>();

            var updatedOrder = await db.Orders
                .Include(order => order.OrderStatusHistories)
                .FirstAsync(order => order.OrderId == orderId);

            Assert.Equal("Confirmed", updatedOrder.OrderStatus);
            Assert.Equal("Pending", updatedOrder.PaymentStatus);

            var history = Assert.Single(updatedOrder.OrderStatusHistories);

            Assert.Equal("Pending", history.PreviousStatus);
            Assert.Equal("Confirmed", history.NewStatus);
            Assert.Equal("test-staff-id", history.ChangedByIdentityUserId);
            Assert.Equal(
                "Order approved for preparation.",
                history.Notes);
        }

        var notification = Assert.Single(notificationService.Notifications);

        Assert.Equal("test-customer-id", notification.IdentityUserId);
        Assert.Equal(orderId.ToString(), notification.OrderId);
        Assert.Equal("Confirmed", notification.Status);
    }

  [Fact]
public async Task AdminCanConfirmPendingOrder()
{
    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            OrderSource = "Online",
            OrderStatus = "Pending",
            PaymentStatus = "Pending",
            CustomerName = "Admin Test Customer",
            CustomerEmail = "admin-test@example.com",
            Subtotal = 50m,
            DiscountAmount = 0m,
            TotalAmount = 50m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Admin");

    var response = await client.PutAsJsonAsync(
        $"/api/staff/orders/{orderId}/status",
        new
        {
            status = "Confirmed",
            notes = "Confirmed by Admin."
        });

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);

    using var verificationScope = _factory.Services.CreateScope();

    var verificationDb = verificationScope.ServiceProvider
        .GetRequiredService<SkinnerCafeDbContext>();

    var updatedOrder = await verificationDb.Orders
        .FirstAsync(order => order.OrderId == orderId);

    Assert.Equal("Confirmed", updatedOrder.OrderStatus);
}

[Fact]
public async Task CustomerCannotUpdateOrderStatus()
{
    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Customer");

    var response = await client.PutAsJsonAsync(
        "/api/staff/orders/1/status",
        new
        {
            status = "Confirmed",
            notes = "Customer attempting to confirm."
        });

    Assert.Equal(HttpStatusCode.Forbidden, response.StatusCode);
}

[Fact]
public async Task StaffCannotSkipOrderStatuses()
{
    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            OrderSource = "Online",
            OrderStatus = "Pending",
            PaymentStatus = "Pending",
            CustomerName = "Transition Test Customer",
            Subtotal = 50m,
            DiscountAmount = 0m,
            TotalAmount = 50m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.PutAsJsonAsync(
        $"/api/staff/orders/{orderId}/status",
        new
        {
            status = "Ready",
            notes = "Attempting to skip stages."
        });

    Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
}

[Fact]
public async Task StaffMarkingOrderReadyNotifiesCustomer()
{
    var notificationService =
        _factory.Services.GetRequiredService<TestOrderNotificationService>();

    notificationService.Clear();

    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            Customer = new CustomerProfile
            {
                IdentityUserId = "test-ready-customer-id",
                FirstName = "Test",
                LastName = "Customer",
                Email = $"ready-{Guid.NewGuid():N}@example.com",
                CreatedAt = DateTime.UtcNow,
                IsActive = true
            },
            OrderSource = "Online",
            OrderStatus = "Preparing",
            PaymentStatus = "Pending",
            CustomerName = "Test Customer",
            Subtotal = 100m,
            DiscountAmount = 0m,
            TotalAmount = 100m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.PutAsJsonAsync(
        $"/api/staff/orders/{orderId}/status",
        new
        {
            status = "Ready",
            notes = "Order ready for collection."
        });

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var updatedOrder = await db.Orders
            .FirstAsync(order => order.OrderId == orderId);

        Assert.Equal("Ready", updatedOrder.OrderStatus);
        Assert.Equal("Pending", updatedOrder.PaymentStatus);
    }

    var notification = Assert.Single(notificationService.Notifications);

    Assert.Equal("test-ready-customer-id", notification.IdentityUserId);
    Assert.Equal(orderId.ToString(), notification.OrderId);
    Assert.Equal("Ready", notification.Status);
}

[Fact]
public async Task StaffMovingOrderToPreparingDoesNotNotifyCustomer()
{
    var notificationService =
        _factory.Services.GetRequiredService<TestOrderNotificationService>();

    notificationService.Clear();

    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            Customer = new CustomerProfile
            {
                IdentityUserId = "test-preparing-customer-id",
                FirstName = "Test",
                LastName = "Customer",
                Email = $"preparing-{Guid.NewGuid():N}@example.com",
                CreatedAt = DateTime.UtcNow,
                IsActive = true
            },
            OrderSource = "Online",
            OrderStatus = "Confirmed",
            PaymentStatus = "Pending",
            CustomerName = "Test Customer",
            Subtotal = 100m,
            DiscountAmount = 0m,
            TotalAmount = 100m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.PutAsJsonAsync(
        $"/api/staff/orders/{orderId}/status",
        new
        {
            status = "Preparing",
            notes = "Preparation started."
        });

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var updatedOrder = await db.Orders
            .FirstAsync(order => order.OrderId == orderId);

        Assert.Equal("Preparing", updatedOrder.OrderStatus);
    }

    Assert.Empty(notificationService.Notifications);
}

[Fact]
public async Task StaffConfirmingGuestOrderDoesNotNotifyCustomer()
{
    var notificationService =
        _factory.Services.GetRequiredService<TestOrderNotificationService>();

    notificationService.Clear();

    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            OrderSource = "Online",
            OrderStatus = "Pending",
            PaymentStatus = "Pending",
            CustomerName = "Guest Customer",
            CustomerEmail = "guest@example.com",
            Subtotal = 100m,
            DiscountAmount = 0m,
            TotalAmount = 100m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.PutAsJsonAsync(
        $"/api/staff/orders/{orderId}/status",
        new
        {
            status = "Confirmed",
            notes = "Guest order confirmed."
        });

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    Assert.Empty(notificationService.Notifications);
}

[Fact]
public async Task StaffCancellingOrderDoesNotNotifyCustomer()
{
    var notificationService =
        _factory.Services.GetRequiredService<TestOrderNotificationService>();

    notificationService.Clear();

    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            Customer = new CustomerProfile
            {
                IdentityUserId = "test-cancel-customer-id",
                FirstName = "Test",
                LastName = "Customer",
                Email = $"cancel-{Guid.NewGuid():N}@example.com",
                CreatedAt = DateTime.UtcNow,
                IsActive = true
            },
            OrderSource = "Online",
            OrderStatus = "Pending",
            PaymentStatus = "Pending",
            CustomerName = "Test Customer",
            Subtotal = 100m,
            DiscountAmount = 0m,
            TotalAmount = 100m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.PutAsJsonAsync(
        $"/api/staff/orders/{orderId}/status",
        new
        {
            status = "Cancelled",
            notes = "Order cancelled."
        });

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var updatedOrder = await db.Orders
            .FirstAsync(order => order.OrderId == orderId);

        Assert.Equal("Cancelled", updatedOrder.OrderStatus);
    }

    Assert.Empty(notificationService.Notifications);
}

[Fact]
public async Task StaffCompletingOrderRecordsCompletedAt()
{
    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            OrderSource = "Online",
            OrderStatus = "Collected",
            PaymentStatus = "Pending",
            CustomerName = "Completion Test Customer",
            Subtotal = 100m,
            DiscountAmount = 0m,
            TotalAmount = 100m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.PutAsJsonAsync(
        $"/api/staff/orders/{orderId}/status",
        new
        {
            status = "Completed",
            notes = "Collection completed."
        });

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);

    using var verificationScope = _factory.Services.CreateScope();

    var verificationDb = verificationScope.ServiceProvider
        .GetRequiredService<SkinnerCafeDbContext>();

    var completedOrder = await verificationDb.Orders
        .FirstAsync(order => order.OrderId == orderId);

    Assert.Equal("Completed", completedOrder.OrderStatus);
    Assert.NotNull(completedOrder.CompletedAt);
}

[Fact]
public async Task InvalidTransitionDoesNotChangeOrderOrHistory()
{
    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            OrderSource = "Online",
            OrderStatus = "Pending",
            PaymentStatus = "Pending",
            CustomerName = "Invalid Transition Customer",
            Subtotal = 100m,
            DiscountAmount = 0m,
            TotalAmount = 100m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.PutAsJsonAsync(
        $"/api/staff/orders/{orderId}/status",
        new
        {
            status = "Ready",
            notes = "Invalid transition attempt."
        });

    Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);

    using var verificationScope = _factory.Services.CreateScope();

    var verificationDb = verificationScope.ServiceProvider
        .GetRequiredService<SkinnerCafeDbContext>();

    var unchangedOrder = await verificationDb.Orders
        .Include(order => order.OrderStatusHistories)
        .FirstAsync(order => order.OrderId == orderId);

    Assert.Equal("Pending", unchangedOrder.OrderStatus);
    Assert.Empty(unchangedOrder.OrderStatusHistories);
}

[Fact]
public async Task StaffCanRetrieveOrderQueue()
{
    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var order = new Order
        {
            OrderNumber = $"TEST-{Guid.NewGuid():N}",
            OrderSource = "Online",
            OrderStatus = "Pending",
            PaymentStatus = "Pending",
            CustomerName = "Queue Test Customer",
            CustomerEmail = "queue-test@example.com",
            CustomerPhone = "0821234567",
            Subtotal = 100m,
            DiscountAmount = 0m,
            TotalAmount = 100m,
            CreatedAt = DateTime.UtcNow
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.GetAsync("/api/staff/orders");

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);

    var orders = await response.Content
        .ReadFromJsonAsync<List<StaffOrderResponse>>();

    Assert.NotNull(orders);

    var retrievedOrder = Assert.Single(
        orders,
        order => order.OrderId == orderId);

    Assert.Equal("Queue Test Customer", retrievedOrder.CustomerName);
    Assert.Equal("Pending", retrievedOrder.OrderStatus);
    Assert.Equal("Pending", retrievedOrder.PaymentStatus);
    Assert.Equal(100m, retrievedOrder.TotalAmount);
}

[Fact]
public async Task AdminCanRetrieveOrderQueue()
{
    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Admin");

    var response = await client.GetAsync("/api/staff/orders");

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);
}

[Fact]
public async Task CustomerCannotRetrieveOrderQueue()
{
    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Customer");

    var response = await client.GetAsync("/api/staff/orders");

    Assert.Equal(HttpStatusCode.Forbidden, response.StatusCode);
}

[Fact]
public async Task UnauthenticatedUserCannotRetrieveOrderQueue()
{
    using var client = _factory.CreateClient();

    var response = await client.GetAsync("/api/staff/orders");

    Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
}


[Fact]
public async Task StaffCanRetrieveOrderQueueWithRelatedDetails()
{
    int orderId;

    using (var scope = _factory.Services.CreateScope())
    {
        var db = scope.ServiceProvider
            .GetRequiredService<SkinnerCafeDbContext>();

        var uniqueId = Guid.NewGuid().ToString("N");

        var category = new Category
        {
            CategoryName = $"Queue Test Category {uniqueId}",
            Description = "Test category",
            DisplayOrder = 1,
            IsActive = true
        };

        var product = new Product
        {
            Category = category,
            ProductName = $"Queue Test Product {uniqueId}",
            Description = "Test product",
            IsStockTracked = false,
            IsActive = true
        };

        var variant = new ProductVariant
        {
            Product = product,
            VariantName = "Regular",
            Price = 50m,
            IsActive = true,
            DisplayOrder = 1
        };

        var addOn = new AddOn
        {
            AddOnName = $"Queue Test AddOn {uniqueId}",
            Price = 10m,
            IsActive = true
        };

        var order = new Order
        {
            OrderNumber = $"TEST-{uniqueId}",
            OrderSource = "Online",
            OrderStatus = "Confirmed",
            PaymentStatus = "Paid",
            CustomerName = "Queue Detail Customer",
            CustomerEmail = "queue-details@example.com",
            CustomerPhone = "0821234567",
            Subtotal = 110m,
            DiscountAmount = 0m,
            TotalAmount = 110m,
            CreatedAt = DateTime.UtcNow,

            OrderItems =
            [
                new OrderItem
                {
                    Variant = variant,
                    ProductNameSnapshot = product.ProductName,
                    VariantNameSnapshot = variant.VariantName,
                    Quantity = 2,
                    UnitPrice = 50m,
                    LineTotal = 100m,

                    OrderItemAddOns =
                    [
                        new OrderItemAddOn
                        {
                            AddOn = addOn,
                            AddOnNameSnapshot = addOn.AddOnName,
                            Quantity = 1,
                            UnitPrice = 10m
                        }
                    ]
                }
            ],

            Payments =
            [
                new Payment
                {
                    PaymentMethod = "Card",
                    PaymentStatus = "Paid",
                    Amount = 110m,
                    PaymentProvider = "Visa",
                    CreatedAt = DateTime.UtcNow,
                    PaidAt = DateTime.UtcNow
                }
            ],

            OrderStatusHistories =
            [
                new OrderStatusHistory
                {
                    PreviousStatus = "Pending",
                    NewStatus = "Confirmed",
                    ChangedByIdentityUserId = "test-staff-id",
                    Notes = "Approved by staff.",
                    ChangedAt = DateTime.UtcNow
                }
            ]
        };

        db.Orders.Add(order);
        await db.SaveChangesAsync();

        orderId = order.OrderId;
    }

    using var client = _factory.CreateClient();

    client.DefaultRequestHeaders.Add("X-Test-Role", "Staff");

    var response = await client.GetAsync("/api/staff/orders");

    Assert.Equal(HttpStatusCode.OK, response.StatusCode);

    var orders = await response.Content
        .ReadFromJsonAsync<List<StaffOrderResponse>>();

    Assert.NotNull(orders);

    var retrievedOrder = Assert.Single(
        orders,
        order => order.OrderId == orderId);

    Assert.Equal("Confirmed", retrievedOrder.OrderStatus);
    Assert.Equal("Paid", retrievedOrder.PaymentStatus);

    var item = Assert.Single(retrievedOrder.Items);

    Assert.Equal("Queue Test Product " + item.ProductName.Split("Queue Test Product ").Last(),
        item.ProductName);
    Assert.Equal("Regular", item.VariantName);
    Assert.Equal(2, item.Quantity);
    Assert.Equal(100m, item.LineTotal);

    var retrievedAddOn = Assert.Single(item.AddOns);

    Assert.StartsWith("Queue Test AddOn ", retrievedAddOn.Name);
    Assert.Equal(1, retrievedAddOn.Quantity);
    Assert.Equal(10m, retrievedAddOn.UnitPrice);

    var payment = Assert.Single(retrievedOrder.Payments);

    Assert.Equal("Card", payment.PaymentMethod);
    Assert.Equal("Paid", payment.PaymentStatus);
    Assert.Equal(110m, payment.Amount);

    var history = Assert.Single(retrievedOrder.StatusHistory);

    Assert.Equal("Pending", history.PreviousStatus);
    Assert.Equal("Confirmed", history.NewStatus);
    Assert.Equal("test-staff-id", history.ChangedByIdentityUserId);
    Assert.Equal("Approved by staff.", history.Notes);
}

}