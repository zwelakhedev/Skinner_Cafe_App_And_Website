
/*
    Skinner Cafe
    Script: 02_CreateSchema.sql
    Purpose: Create the initial menu and add-on tables.
    
    Run against the SkinnerCafe database.
*/

USE SkinnerCafe;
GO

/* 1. Menu categories */
CREATE TABLE dbo.Categories
(
    CategoryID INT IDENTITY(1,1) NOT NULL,
    CategoryName NVARCHAR(100) NOT NULL,
    Description NVARCHAR(250) NULL,
    DisplayOrder INT NOT NULL CONSTRAINT DF_Categories_DisplayOrder DEFAULT (0),
    IsActive BIT NOT NULL CONSTRAINT DF_Categories_IsActive DEFAULT (1),

    CONSTRAINT PK_Categories PRIMARY KEY (CategoryID),
    CONSTRAINT UQ_Categories_CategoryName UNIQUE (CategoryName),
    CONSTRAINT CK_Categories_DisplayOrder CHECK (DisplayOrder >= 0)
);
GO

/* 2. Products */
CREATE TABLE dbo.Products
(
    ProductID INT IDENTITY(1,1) NOT NULL,
    CategoryID INT NOT NULL,
    ProductName NVARCHAR(150) NOT NULL,
    Description NVARCHAR(500) NULL,
    IsStockTracked BIT NOT NULL CONSTRAINT DF_Products_IsStockTracked DEFAULT (0),
    IsActive BIT NOT NULL CONSTRAINT DF_Products_IsActive DEFAULT (1),
    CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Products_CreatedAt DEFAULT (SYSUTCDATETIME()),
    UpdatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_Products_UpdatedAt DEFAULT (SYSUTCDATETIME()),

    CONSTRAINT PK_Products PRIMARY KEY (ProductID),
    CONSTRAINT FK_Products_Categories
        FOREIGN KEY (CategoryID) REFERENCES dbo.Categories(CategoryID),
    CONSTRAINT UQ_Products_Category_ProductName UNIQUE (CategoryID, ProductName)
);
GO

/* 3. Product variants and prices */
CREATE TABLE dbo.ProductVariants
(
    VariantID INT IDENTITY(1,1) NOT NULL,
    ProductID INT NOT NULL,
    VariantName NVARCHAR(50) NOT NULL,
    Price DECIMAL(10,2) NOT NULL,
    IsActive BIT NOT NULL CONSTRAINT DF_ProductVariants_IsActive DEFAULT (1),
    DisplayOrder INT NOT NULL CONSTRAINT DF_ProductVariants_DisplayOrder DEFAULT (0),

    CONSTRAINT PK_ProductVariants PRIMARY KEY (VariantID),
    CONSTRAINT FK_ProductVariants_Products
        FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID),
    CONSTRAINT UQ_ProductVariants_Product_Variant UNIQUE (ProductID, VariantName),
    CONSTRAINT CK_ProductVariants_Price CHECK (Price >= 0),
    CONSTRAINT CK_ProductVariants_DisplayOrder CHECK (DisplayOrder >= 0)
);
GO

/* 4. Add-ons */
CREATE TABLE dbo.AddOns
(
    AddOnID INT IDENTITY(1,1) NOT NULL,
    AddOnName NVARCHAR(100) NOT NULL,
    Price DECIMAL(10,2) NOT NULL,
    IsActive BIT NOT NULL CONSTRAINT DF_AddOns_IsActive DEFAULT (1),
    CreatedAt DATETIME2(0) NOT NULL CONSTRAINT DF_AddOns_CreatedAt DEFAULT (SYSUTCDATETIME()),

    CONSTRAINT PK_AddOns PRIMARY KEY (AddOnID),
    CONSTRAINT UQ_AddOns_AddOnName UNIQUE (AddOnName),
    CONSTRAINT CK_AddOns_Price CHECK (Price >= 0)
);
GO

/* 5. Products eligible for specific add-ons */
CREATE TABLE dbo.ProductAddOns
(
    ProductID INT NOT NULL,
    AddOnID INT NOT NULL,

    CONSTRAINT PK_ProductAddOns PRIMARY KEY (ProductID, AddOnID),
    CONSTRAINT FK_ProductAddOns_Products
        FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID),
    CONSTRAINT FK_ProductAddOns_AddOns
        FOREIGN KEY (AddOnID) REFERENCES dbo.AddOns(AddOnID)
);
GO

PRINT 'Initial menu and add-on tables created successfully.';
GO