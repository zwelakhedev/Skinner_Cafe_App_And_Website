using System;
using System.Collections.Generic;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Models;

namespace SkinnerCafe.API.Data;

public partial class SkinnerCafeDbContext : IdentityDbContext<ApplicationUser>
{
    public SkinnerCafeDbContext(DbContextOptions<SkinnerCafeDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<AddOn> AddOns { get; set; }

    public virtual DbSet<Category> Categories { get; set; }

    public virtual DbSet<CustomerProfile> CustomerProfiles { get; set; }

    public virtual DbSet<Order> Orders { get; set; }

    public virtual DbSet<OrderItem> OrderItems { get; set; }

    public virtual DbSet<OrderItemAddOn> OrderItemAddOns { get; set; }

    public virtual DbSet<OrderStatusHistory> OrderStatusHistories { get; set; }

    public virtual DbSet<Payment> Payments { get; set; }

    public virtual DbSet<Product> Products { get; set; }

    public virtual DbSet<ProductInventory> ProductInventories { get; set; }

    public virtual DbSet<ProductVariant> ProductVariants { get; set; }

    public virtual DbSet<StockDelivery> StockDeliveries { get; set; }

    public virtual DbSet<StockDeliveryItem> StockDeliveryItems { get; set; }

    public virtual DbSet<StockMovement> StockMovements { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        modelBuilder.Entity<AddOn>(entity =>
        {
            entity.HasIndex(e => e.AddOnName, "UQ_AddOns_AddOnName").IsUnique();

            entity.Property(e => e.AddOnId).HasColumnName("AddOnID");
            entity.Property(e => e.AddOnName).HasMaxLength(100);
            entity.Property(e => e.CreatedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.IsActive).HasDefaultValue(true);
            entity.Property(e => e.Price).HasColumnType("decimal(10, 2)");
        });

        modelBuilder.Entity<Category>(entity =>
        {
            entity.HasIndex(e => e.CategoryName, "UQ_Categories_CategoryName").IsUnique();

            entity.Property(e => e.CategoryId).HasColumnName("CategoryID");
            entity.Property(e => e.CategoryName).HasMaxLength(100);
            entity.Property(e => e.Description).HasMaxLength(250);
            entity.Property(e => e.IsActive).HasDefaultValue(true);
        });

        modelBuilder.Entity<CustomerProfile>(entity =>
        {
            entity.HasKey(e => e.CustomerId);

            entity.HasIndex(e => e.Email, "UQ_CustomerProfiles_Email").IsUnique();

            entity.HasIndex(e => e.IdentityUserId, "UQ_CustomerProfiles_IdentityUserId").IsUnique();

            entity.Property(e => e.CustomerId).HasColumnName("CustomerID");
            entity.Property(e => e.CreatedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.Email).HasMaxLength(256);
            entity.Property(e => e.FirstName).HasMaxLength(100);
            entity.Property(e => e.IsActive).HasDefaultValue(true);
            entity.Property(e => e.LastName).HasMaxLength(100);
            entity.Property(e => e.PhoneNumber).HasMaxLength(30);
        });

        modelBuilder.Entity<Order>(entity =>
        {
            entity.HasIndex(e => new { e.CustomerId, e.CreatedAt }, "IX_Orders_CustomerID_CreatedAt").IsDescending(false, true);

            entity.HasIndex(e => new { e.OrderStatus, e.CreatedAt }, "IX_Orders_OrderStatus_CreatedAt").IsDescending(false, true);

            entity.HasIndex(e => e.OrderNumber, "UQ_Orders_OrderNumber").IsUnique();

            entity.Property(e => e.OrderId).HasColumnName("OrderID");
            entity.Property(e => e.CompletedAt).HasPrecision(0);
            entity.Property(e => e.CreatedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.CustomerEmail).HasMaxLength(256);
            entity.Property(e => e.CustomerId).HasColumnName("CustomerID");
            entity.Property(e => e.CustomerName).HasMaxLength(200);
            entity.Property(e => e.CustomerPhone).HasMaxLength(30);
            entity.Property(e => e.DiscountAmount).HasColumnType("decimal(10, 2)");
            entity.Property(e => e.OrderNumber).HasMaxLength(30);
            entity.Property(e => e.OrderSource).HasMaxLength(10);
            entity.Property(e => e.OrderStatus)
                .HasMaxLength(20)
                .HasDefaultValue("Pending");
            entity.Property(e => e.PaymentStatus)
                .HasMaxLength(20)
                .HasDefaultValue("Pending");
            entity.Property(e => e.Subtotal).HasColumnType("decimal(10, 2)");
            entity.Property(e => e.TotalAmount).HasColumnType("decimal(10, 2)");

            entity.HasOne(d => d.Customer).WithMany(p => p.Orders)
                .HasForeignKey(d => d.CustomerId)
                .HasConstraintName("FK_Orders_CustomerProfiles");
        });

        modelBuilder.Entity<OrderItem>(entity =>
        {
            entity.HasIndex(e => e.OrderId, "IX_OrderItems_OrderID");

            entity.Property(e => e.OrderItemId).HasColumnName("OrderItemID");
            entity.Property(e => e.LineTotal).HasColumnType("decimal(10, 2)");
            entity.Property(e => e.OrderId).HasColumnName("OrderID");
            entity.Property(e => e.ProductNameSnapshot).HasMaxLength(150);
            entity.Property(e => e.UnitPrice).HasColumnType("decimal(10, 2)");
            entity.Property(e => e.VariantId).HasColumnName("VariantID");
            entity.Property(e => e.VariantNameSnapshot).HasMaxLength(50);

            entity.HasOne(d => d.Order).WithMany(p => p.OrderItems)
                .HasForeignKey(d => d.OrderId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_OrderItems_Orders");

            entity.HasOne(d => d.Variant).WithMany(p => p.OrderItems)
                .HasForeignKey(d => d.VariantId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_OrderItems_ProductVariants");
        });

        modelBuilder.Entity<OrderItemAddOn>(entity =>
        {
            entity.Property(e => e.OrderItemAddOnId).HasColumnName("OrderItemAddOnID");
            entity.Property(e => e.AddOnId).HasColumnName("AddOnID");
            entity.Property(e => e.AddOnNameSnapshot).HasMaxLength(100);
            entity.Property(e => e.OrderItemId).HasColumnName("OrderItemID");
            entity.Property(e => e.Quantity).HasDefaultValue(1);
            entity.Property(e => e.UnitPrice).HasColumnType("decimal(10, 2)");

            entity.HasOne(d => d.AddOn).WithMany(p => p.OrderItemAddOns)
                .HasForeignKey(d => d.AddOnId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_OrderItemAddOns_AddOns");

            entity.HasOne(d => d.OrderItem).WithMany(p => p.OrderItemAddOns)
                .HasForeignKey(d => d.OrderItemId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_OrderItemAddOns_OrderItems");
        });

        modelBuilder.Entity<OrderStatusHistory>(entity =>
        {
            entity.ToTable("OrderStatusHistory");

            entity.HasIndex(e => new { e.OrderId, e.ChangedAt }, "IX_OrderStatusHistory_OrderID_ChangedAt").IsDescending(false, true);

            entity.Property(e => e.OrderStatusHistoryId).HasColumnName("OrderStatusHistoryID");
            entity.Property(e => e.ChangedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.ChangedByIdentityUserId).HasMaxLength(450);
            entity.Property(e => e.NewStatus).HasMaxLength(20);
            entity.Property(e => e.Notes).HasMaxLength(300);
            entity.Property(e => e.OrderId).HasColumnName("OrderID");
            entity.Property(e => e.PreviousStatus).HasMaxLength(20);

            entity.HasOne(d => d.Order).WithMany(p => p.OrderStatusHistories)
                .HasForeignKey(d => d.OrderId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_OrderStatusHistory_Orders");
        });

        modelBuilder.Entity<Payment>(entity =>
        {
            entity.HasIndex(e => new { e.OrderId, e.CreatedAt }, "IX_Payments_OrderID_CreatedAt").IsDescending(false, true);

            entity.HasIndex(e => new { e.OrderId, e.IdempotencyKey }, "UX_Payments_Order_IdempotencyKey")
                .IsUnique()
                .HasFilter("([IdempotencyKey] IS NOT NULL)");

            entity.Property(e => e.PaymentId).HasColumnName("PaymentID");
            entity.Property(e => e.Amount).HasColumnType("decimal(10, 2)");
            entity.Property(e => e.CreatedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.IdempotencyKey).HasMaxLength(100);
            entity.Property(e => e.OrderId).HasColumnName("OrderID");
            entity.Property(e => e.PaidAt).HasPrecision(0);
            entity.Property(e => e.PaymentMethod).HasMaxLength(20);
            entity.Property(e => e.PaymentStatus)
                .HasMaxLength(20)
                .HasDefaultValue("Pending");
            entity.Property(e => e.ProviderReference).HasMaxLength(200);

            entity.HasOne(d => d.Order).WithMany(p => p.Payments)
                .HasForeignKey(d => d.OrderId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Payments_Orders");
        });

        modelBuilder.Entity<Product>(entity =>
        {
            entity.HasIndex(e => new { e.CategoryId, e.ProductName }, "UQ_Products_Category_ProductName").IsUnique();

            entity.Property(e => e.ProductId).HasColumnName("ProductID");
            entity.Property(e => e.CategoryId).HasColumnName("CategoryID");
            entity.Property(e => e.CreatedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.Description).HasMaxLength(500);
            entity.Property(e => e.IsActive).HasDefaultValue(true);
            entity.Property(e => e.ProductName).HasMaxLength(150);
            entity.Property(e => e.UpdatedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");

            entity.HasOne(d => d.Category).WithMany(p => p.Products)
                .HasForeignKey(d => d.CategoryId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Products_Categories");

            entity.HasMany(d => d.AddOns).WithMany(p => p.Products)
                .UsingEntity<Dictionary<string, object>>(
                    "ProductAddOn",
                    r => r.HasOne<AddOn>().WithMany()
                        .HasForeignKey("AddOnId")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("FK_ProductAddOns_AddOns"),
                    l => l.HasOne<Product>().WithMany()
                        .HasForeignKey("ProductId")
                        .OnDelete(DeleteBehavior.ClientSetNull)
                        .HasConstraintName("FK_ProductAddOns_Products"),
                    j =>
                    {
                        j.HasKey("ProductId", "AddOnId");
                        j.ToTable("ProductAddOns");
                        j.IndexerProperty<int>("ProductId").HasColumnName("ProductID");
                        j.IndexerProperty<int>("AddOnId").HasColumnName("AddOnID");
                    });
        });

        modelBuilder.Entity<ProductInventory>(entity =>
        {
            entity.HasKey(e => e.ProductId);

            entity.ToTable("ProductInventory");

            entity.Property(e => e.ProductId)
                .ValueGeneratedNever()
                .HasColumnName("ProductID");
            entity.Property(e => e.LowStockThreshold).HasDefaultValue(5);
            entity.Property(e => e.UpdatedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");

            entity.HasOne(d => d.Product).WithOne(p => p.ProductInventory)
                .HasForeignKey<ProductInventory>(d => d.ProductId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_ProductInventory_Products");
        });

        modelBuilder.Entity<ProductVariant>(entity =>
        {
            entity.HasKey(e => e.VariantId);

            entity.HasIndex(e => new { e.ProductId, e.VariantName }, "UQ_ProductVariants_Product_Variant").IsUnique();

            entity.Property(e => e.VariantId).HasColumnName("VariantID");
            entity.Property(e => e.IsActive).HasDefaultValue(true);
            entity.Property(e => e.Price).HasColumnType("decimal(10, 2)");
            entity.Property(e => e.ProductId).HasColumnName("ProductID");
            entity.Property(e => e.VariantName).HasMaxLength(50);

            entity.HasOne(d => d.Product).WithMany(p => p.ProductVariants)
                .HasForeignKey(d => d.ProductId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_ProductVariants_Products");
        });

        modelBuilder.Entity<StockDelivery>(entity =>
        {
            entity.Property(e => e.StockDeliveryId).HasColumnName("StockDeliveryID");
            entity.Property(e => e.DeliveryReference).HasMaxLength(100);
            entity.Property(e => e.Notes).HasMaxLength(300);
            entity.Property(e => e.ReceivedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.ReceivedByIdentityUserId).HasMaxLength(450);
            entity.Property(e => e.SupplierName).HasMaxLength(200);
        });

        modelBuilder.Entity<StockDeliveryItem>(entity =>
        {
            entity.Property(e => e.StockDeliveryItemId).HasColumnName("StockDeliveryItemID");
            entity.Property(e => e.ProductId).HasColumnName("ProductID");
            entity.Property(e => e.StockDeliveryId).HasColumnName("StockDeliveryID");

            entity.HasOne(d => d.Product).WithMany(p => p.StockDeliveryItems)
                .HasForeignKey(d => d.ProductId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_StockDeliveryItems_Products");

            entity.HasOne(d => d.StockDelivery).WithMany(p => p.StockDeliveryItems)
                .HasForeignKey(d => d.StockDeliveryId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_StockDeliveryItems_StockDeliveries");
        });

        modelBuilder.Entity<StockMovement>(entity =>
        {
            entity.HasIndex(e => new { e.ProductId, e.CreatedAt }, "IX_StockMovements_ProductID_CreatedAt").IsDescending(false, true);

            entity.HasIndex(e => new { e.OrderId, e.ProductId }, "UX_StockMovements_Order_Product_Sale")
                .IsUnique()
                .HasFilter("([MovementType]='Sale' AND [OrderID] IS NOT NULL)");

            entity.Property(e => e.StockMovementId).HasColumnName("StockMovementID");
            entity.Property(e => e.ChangedByIdentityUserId).HasMaxLength(450);
            entity.Property(e => e.CreatedAt)
                .HasPrecision(0)
                .HasDefaultValueSql("(sysutcdatetime())");
            entity.Property(e => e.MovementType).HasMaxLength(20);
            entity.Property(e => e.OrderId).HasColumnName("OrderID");
            entity.Property(e => e.ProductId).HasColumnName("ProductID");
            entity.Property(e => e.Reason).HasMaxLength(300);
            entity.Property(e => e.StockDeliveryId).HasColumnName("StockDeliveryID");
            entity.Property(e => e.StockDeliveryItemId).HasColumnName("StockDeliveryItemID");

            entity.HasOne(d => d.Order).WithMany(p => p.StockMovements)
                .HasForeignKey(d => d.OrderId)
                .HasConstraintName("FK_StockMovements_Orders");

            entity.HasOne(d => d.Product).WithMany(p => p.StockMovements)
                .HasForeignKey(d => d.ProductId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_StockMovements_Products");

            entity.HasOne(d => d.StockDelivery).WithMany(p => p.StockMovements)
                .HasForeignKey(d => d.StockDeliveryId)
                .HasConstraintName("FK_StockMovements_StockDeliveries");

            entity.HasOne(d => d.StockDeliveryItem).WithMany(p => p.StockMovements)
                .HasForeignKey(d => d.StockDeliveryItemId)
                .HasConstraintName("FK_StockMovements_StockDeliveryItems");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
