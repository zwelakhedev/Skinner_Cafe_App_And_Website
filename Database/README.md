# Skinner Cafe Database

## 1. Overview

The Skinner Cafe application uses a Microsoft SQL Server database named `SkinnerCafe`.

The database stores the application's business data, including:

- Products and menu categories
- Product variants and add-ons
- Customer profiles
- Orders and order items
- Payments
- Inventory and stock movements
- Stock deliveries
- User authentication and identity information

The database is accessed through the ASP.NET Core Web API. The React website and Android application communicate with the API rather than connecting directly to SQL Server.

## 2. Database Architecture

The application follows this architecture:

```text
React Website ──────┐
                    │
                    ▼
             ASP.NET Core API
                    │
                    ▼
             SQL Server Database
                    ▲
                    │
Android Application ┘
```

**Important:** The `Database` directory in this repository contains SQL scripts and documentation. It is not the physical SQL Server database itself.

SQL Server manages the actual database files separately.

## 3. Repository Structure

```text
Database/
│
├── scripts/
│   ├── 01_CreateDatabase.sql
│   ├── 02_CreateSchema.sql
│   ├── 03_CreateAccountsAndOrders.sql
│   ├── 04_CreatePaymentsAndInventory.sql
│   └── 05_SeedMenuData.sql
│
├── schema/
│   └── README.md
│
├── seed-data/
│
├── AddIdentityAuthentication.sql
│
└── README.md
```

### Directory descriptions

| Directory/File | Purpose |
|---|---|
| `scripts/` | SQL scripts for creating the original database structure and inserting initial menu data. |
| `schema/` | Database schema reference documentation, including the verified table structure and relationships. |
| `seed-data/` | Reserved for additional database seed data files if required in the future. |
| `AddIdentityAuthentication.sql` | Earlier standalone SQL approach for creating authentication-related database objects. |
| `README.md` | Main database documentation and setup instructions. |

For more information about the actual database tables and relationships, see [`schema/README.md`](schema/README.md).

## 4. Database Technology

| Component | Technology |
|---|---|
| Database engine | Microsoft SQL Server |
| Database name | SkinnerCafe |
| Data access | Entity Framework Core |
| API framework | ASP.NET Core Web API |
| Database provider | Microsoft.EntityFrameworkCore.SqlServer |
| EF Core version | 9.0.21 |
| Target framework | .NET 9 |

The development environment uses SQL Server Express/LocalDB.

## 5. SQL Scripts

The `scripts` directory contains the original database setup scripts.

| Script | Purpose |
|---|---|
| `01_CreateDatabase.sql` | Creates the SkinnerCafe database. |
| `02_CreateSchema.sql` | Creates the initial database schema. |
| `03_CreateAccountsAndOrders.sql` | Creates account and order-related business tables. |
| `04_CreatePaymentsAndInventory.sql` | Creates payment and inventory-related tables. |
| `05_SeedMenuData.sql` | Inserts initial menu and product data. |

**Important:** These scripts represent the original database setup process. They should not automatically be executed again against an existing database.

Some scripts contain ordinary `CREATE TABLE` and other creation statements that may fail if their objects already exist.

Always verify the database state before executing database scripts.

## 6. Entity Framework Core Migrations

The ASP.NET Core API uses Entity Framework Core migrations to manage subsequent database model changes.

The migration files are located in:

```text
API/
└── SkinnerCafe.API/
    └── Data/
        └── Migrations/
```

Current migrations include:

| Migration | Description |
|---|---|
| `20261027135341_AddIdentityAuthentication` | Introduces ASP.NET Core Identity authentication support. |
| `20261102220344_AddCustomerProfilePictureUrl` | Adds customer profile picture URL support. |

Both migrations have been applied to the development database.

The migration history is recorded in the SQL Server table:

```text
dbo.__EFMigrationsHistory
```

### Managing migrations

Run Entity Framework commands from the API project directory or specify the project explicitly.

To create a new migration:

```powershell
dotnet ef migrations add MigrationName
```

To apply pending migrations:

```powershell
dotnet ef database update
```

To check the available migrations:

```powershell
dotnet ef migrations list
```

**Database change policy:** Entity Framework Core migrations are the authoritative mechanism for managing changes to the API's EF Core model and its corresponding database schema.

Avoid maintaining two independent processes for updating the same database objects.

## 7. Authentication and Identity

The API uses ASP.NET Core Identity for customer authentication.

Identity-related database objects are managed through Entity Framework Core migrations.

The standalone `AddIdentityAuthentication.sql` file represents an alternative manual SQL implementation. It should be treated as a historical/reference script, not as an additional setup step for the existing database.

Do not execute it against the current database without first comparing its contents with the existing migrations and database objects.

## 8. Database Configuration

The API obtains its database connection through its application configuration.

Connection strings should be configured using local development settings, environment variables, or .NET user secrets.

Do not commit passwords, credentials, or production connection strings to GitHub.

Example connection string format:

```text
Server=YOUR_SERVER;
Database=SkinnerCafe;
Trusted_Connection=True;
TrustServerCertificate=True;
```

Replace `YOUR_SERVER` with the appropriate SQL Server instance.

## 9. Development Setup

For a new development environment, the general setup process is:

1. Install a compatible SQL Server instance.
2. Open the solution in Visual Studio.
3. Configure the API database connection string.
4. Create the initial business database structure using the reviewed SQL setup scripts.
5. Apply the required Entity Framework Core migrations.
6. Insert the required initial menu data.
7. Run the ASP.NET Core API.
8. Verify the database connection using the API health endpoint.

The API health endpoint is:

```text
/api/health
```

A successful database connection returns a response similar to:

```json
{
  "status": "Connected",
  "database": "SkinnerCafe"
}
```

Follow the project's current migration and setup instructions when recreating an existing development environment. Do not apply the historical authentication SQL script in addition to the existing EF Core migrations.

## 10. Verified Database Structure

The current development database was inspected using SQL Server system catalog views.

The inspection confirmed 23 tables in total:

| Table category | Number of tables |
|---|---:|
| Business tables | 15 |
| ASP.NET Core Identity tables | 7 |
| EF Core migration history | 1 |
| **Total** | **23** |

The database contains 24 foreign key relationships.

The business schema includes products, categories, variants, add-ons, customer profiles, orders, order items, payments, inventory, stock deliveries and stock movements.

The Identity schema contains the standard ASP.NET Core Identity tables for users, roles, claims, logins and tokens.

For a detailed table-by-table reference, see [`schema/README.md`](schema/README.md).

### Customer and authentication relationship

`CustomerProfiles.IdentityUserId` stores the associated ASP.NET Core Identity user identifier.

The column is required and uniquely indexed.

**Important:** The current database does not enforce a foreign key between `CustomerProfiles.IdentityUserId` and `AspNetUsers.Id`. The association is maintained by the application rather than a database-enforced relationship.

The `CustomerProfiles.ProfilePictureUrl` column is nullable and supports customer profile picture URLs or relative paths.

### Order design

The `Orders.CustomerID` column is nullable to support guest checkout.

Customer name, email and phone details are also stored on the order. This allows order records to retain customer details independently of subsequent profile changes.

Order items store product and variant name snapshots, while order add-ons store their names and prices at the time of ordering.

### Inventory design

Inventory is tracked at product level through `ProductInventory`.

The `Products.IsStockTracked` field determines whether a product is subject to stock tracking.

Stock deliveries and stock movements provide an audit trail of incoming stock, sales and adjustments.

## 11. Database Safety and Maintenance

The following practices should be followed:

- Back up the database before performing structural changes.
- Never commit passwords or sensitive connection information.
- Use Entity Framework Core migrations for model changes.
- Review SQL scripts before executing them.
- Do not rerun initial database creation scripts against an existing database.
- Do not manually delete migration history records.
- Verify the API database connection after database changes.
- Keep development and production database configurations separate.

## 12. Current Development Status

The Skinner Cafe database has been created and connected to the ASP.NET Core API.

The following have been verified:

- The `SkinnerCafe` database exists.
- The original business database tables have been created.
- Initial menu data has been inserted.
- ASP.NET Core Identity migrations have been applied.
- The customer profile picture URL migration has been applied.
- The database contains 23 tables and 24 foreign key relationships.
- Guest checkout support is present.
- The API health endpoint successfully reports a database connection.
- Database schema documentation has been added under `Database/schema/`.

This documentation should be updated as additional database features, migrations, and deployment environments are introduced.