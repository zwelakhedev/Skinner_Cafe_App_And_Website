# Skinner Cafe Database Schema Reference

## 1. Overview

This document describes the verified SQL Server database structure for the Skinner Cafe application.

**Database:** `SkinnerCafe`  
**Database engine:** Microsoft SQL Server  
**Data access:** Entity Framework Core 9.0.21

The database contains 23 tables:

- 15 business tables.
- 7 ASP.NET Core Identity tables.
- 1 Entity Framework migration history table.

## 2. Business Tables

| Table | Purpose |
|---|---|
| Categories | Stores menu categories. |
| Products | Stores menu products. |
| ProductVariants | Stores product sizes or variations and their prices. |
| AddOns | Stores optional product additions. |
| ProductAddOns | Links products to available add-ons. |
| ProductInventory | Stores product stock quantities. |
| CustomerProfiles | Stores customer profile information. |
| Orders | Stores customer and guest orders. |
| OrderItems | Stores individual items belonging to orders. |
| OrderItemAddOns | Stores selected add-ons for order items. |
| OrderStatusHistory | Records order status changes. |
| Payments | Stores payment records and payment status. |
| StockDeliveries | Records incoming stock deliveries. |
| StockDeliveryItems | Stores products included in stock deliveries. |
| StockMovements | Records inventory movements and adjustments. |

## 3. Identity Tables

ASP.NET Core Identity manages authentication and role-related information through these tables:

| Table | Purpose |
|---|---|
| AspNetUsers | Stores user account information. |
| AspNetRoles | Stores application roles. |
| AspNetUserRoles | Links users to roles. |
| AspNetUserClaims | Stores user claims. |
| AspNetRoleClaims | Stores role claims. |
| AspNetUserLogins | Stores external login information. |
| AspNetUserTokens | Stores authentication tokens. |

## 4. Main Relationships

The database contains the following main relationships:

- Categories -> Products
- Products -> ProductVariants
- Products <-> AddOns (through ProductAddOns)
- Products -> ProductInventory
- CustomerProfiles -> Orders
- Orders -> OrderItems
- OrderItems -> OrderItemAddOns
- Orders -> Payments
- Orders -> OrderStatusHistory
- Products -> StockDeliveryItems
- StockDeliveries -> StockDeliveryItems
- Products -> StockMovements
- Orders -> StockMovements
- StockDeliveries -> StockMovements
- StockDeliveryItems -> StockMovements

ASP.NET Core Identity also maintains relationships between users, roles, claims, logins and tokens.

## 5. Customer and Authentication Relationship

`CustomerProfiles.IdentityUserId` stores the associated ASP.NET Core Identity user identifier.

The column is required and uniquely indexed.

**Important:** The current database does not enforce a foreign key between `CustomerProfiles.IdentityUserId` and `AspNetUsers.Id`. The association is maintained by the application rather than a database-enforced relationship.

The `CustomerProfiles.ProfilePictureUrl` column is nullable and stores the customer's profile picture URL or relative path.

## 6. Order Design

The `Orders.CustomerID` column is nullable to support guest checkout.

Customer name, email and phone details are also stored on the order. This allows order records to retain customer details independently of subsequent profile changes.

Order items store product and variant name snapshots, while order add-ons store their names and prices at the time of ordering.

## 7. Inventory Design

Inventory is tracked at product level through `ProductInventory`.

The `Products.IsStockTracked` field determines whether a product is subject to stock tracking.

Stock deliveries and stock movements provide an audit trail of incoming stock, sales and adjustments.

## 8. Migration Management

The API manages subsequent schema changes using Entity Framework Core migrations.

The current applied migrations are:

| Migration | Purpose |
|---|---|
| 20261027135341_AddIdentityAuthentication | Adds ASP.NET Core Identity tables. |
| 20261102220344_AddCustomerProfilePictureUrl | Adds ProfilePictureUrl to CustomerProfiles. |

The migration history is stored in `dbo.__EFMigrationsHistory`.

The EF Core model and migrations should remain the authoritative source for changes to the API-managed database schema.

## 9. Verification

The database structure was inspected using SQL Server system catalog views.

The inspection confirmed:

- 23 database tables.
- 24 foreign key relationships.
- Both listed EF Core migrations applied.
- Customer profile picture support.
- Guest checkout support.
- ASP.NET Core Identity tables and relationships.

This reference should be updated when new tables, relationships or migrations are introduced.