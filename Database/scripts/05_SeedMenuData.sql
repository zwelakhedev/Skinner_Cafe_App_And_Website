/*
    Skinner Cafe
    Script: 05_SeedMenuData.sql
    Purpose: Insert the initial menu, product variants, prices and add-ons.

    Currency: ZAR
    Safe to rerun: existing categories, products, variants and add-ons
    are not duplicated.
*/

USE SkinnerCafe;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* ==========================================
       1. MENU CATEGORIES
       ========================================== */

    DECLARE @Categories TABLE
    (
        CategoryName NVARCHAR(100),
        DisplayOrder INT
    );

    INSERT INTO @Categories (CategoryName, DisplayOrder)
    VALUES
        (N'Bakery', 1),
        (N'Sweet Treats', 2),
        (N'Coffee', 3),
        (N'Freezos', 4),
        (N'Iced Lattes', 5),
        (N'Tea', 6),
        (N'Soft Drinks', 7),
        (N'Breakfast', 8);

    INSERT INTO dbo.Categories (CategoryName, DisplayOrder)
    SELECT c.CategoryName, c.DisplayOrder
    FROM @Categories c
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.Categories existing
        WHERE existing.CategoryName = c.CategoryName
    );


    /* ==========================================
       2. MENU PRODUCTS AND VARIANTS

       Each row represents a product/size combination.
       Products with no size choices use Standard.
       ========================================== */

    DECLARE @Menu TABLE
    (
        CategoryName NVARCHAR(100) NOT NULL,
        ProductName NVARCHAR(150) NOT NULL,
        Description NVARCHAR(500) NULL,
        VariantName NVARCHAR(50) NOT NULL,
        Price DECIMAL(10,2) NOT NULL,
        VariantOrder INT NOT NULL
    );

    INSERT INTO @Menu
    (
        CategoryName,
        ProductName,
        Description,
        VariantName,
        Price,
        VariantOrder
    )
    VALUES

    /* ---------- BAKERY ---------- */

    (N'Bakery', N'Blueberry Muffin', NULL, N'Standard', 35.00, 1),
    (N'Bakery', N'Lemon & Poppyseed Muffin', NULL, N'Standard', 35.00, 1),
    (N'Bakery', N'Double Choc Chip Muffin', NULL, N'Standard', 35.00, 1),

    (N'Bakery', N'Plain Croissant', NULL, N'Standard', 40.00, 1),
    (N'Bakery', N'Almond Croissant', NULL, N'Standard', 50.00, 1),
    (N'Bakery', N'Chocolate Croissant', NULL, N'Standard', 50.00, 1),

    /* ---------- SWEET TREATS ---------- */

    (
        N'Sweet Treats',
        N'Strawberry Cheesecake',
        N'Buttery Biscuit Base, Strawberry Compote & Fresh Strawberries',
        N'Standard', 65.00, 1
    ),
    (
        N'Sweet Treats',
        N'Salted Caramel Cheesecake',
        N'Golden Biscuit Base, Salted Caramel & Drizzle Of Sea Salt',
        N'Standard', 65.00, 1
    ),
    (
        N'Sweet Treats',
        N'Chocolate Ganache Brownie',
        NULL, N'Standard', 55.00, 1
    ),
    (
        N'Sweet Treats',
        N'Cookie Dough Chocolate Chip',
        NULL, N'Standard', 40.00, 1
    ),
    (
        N'Sweet Treats',
        N'Skinner Cookie',
        NULL, N'Standard', 40.00, 1
    ),

    /* ---------- COFFEE ---------- */

    (N'Coffee', N'Cappuccino', NULL, N'Small', 30.00, 1),
    (N'Coffee', N'Cappuccino', NULL, N'Medium', 35.00, 2),
    (N'Coffee', N'Cappuccino', NULL, N'Large', 40.00, 3),

    (N'Coffee', N'Caffe Latte', NULL, N'Small', 30.00, 1),
    (N'Coffee', N'Caffe Latte', NULL, N'Medium', 35.00, 2),
    (N'Coffee', N'Caffe Latte', NULL, N'Large', 40.00, 3),

    (N'Coffee', N'Flat White', NULL, N'Small', 25.00, 1),
    (N'Coffee', N'Flat White', NULL, N'Medium', 30.00, 2),
    (N'Coffee', N'Flat White', NULL, N'Large', 35.00, 3),

    (N'Coffee', N'Cortado', NULL, N'Small', 35.00, 1),

    (N'Coffee', N'Caffe Mocha', NULL, N'Small', 45.00, 1),
    (N'Coffee', N'Caffe Mocha', NULL, N'Medium', 50.00, 2),
    (N'Coffee', N'Caffe Mocha', NULL, N'Large', 55.00, 3),

    (N'Coffee', N'Americano', NULL, N'Small', 25.00, 1),
    (N'Coffee', N'Americano', NULL, N'Medium', 30.00, 2),
    (N'Coffee', N'Americano', NULL, N'Large', 35.00, 3),

    (N'Coffee', N'Espresso', NULL, N'Small', 25.00, 1),
    (N'Coffee', N'Espresso', NULL, N'Medium', 30.00, 2),

    (N'Coffee', N'Hot Chocolate', NULL, N'Small', 40.00, 1),
    (N'Coffee', N'Hot Chocolate', NULL, N'Medium', 45.00, 2),
    (N'Coffee', N'Hot Chocolate', NULL, N'Large', 50.00, 3),

    (N'Coffee', N'Chai Latte', NULL, N'Small', 30.00, 1),
    (N'Coffee', N'Chai Latte', NULL, N'Medium', 40.00, 2),
    (N'Coffee', N'Chai Latte', NULL, N'Large', 45.00, 3),

    (N'Coffee', N'Spanish Latte', N'Espresso, Coconut & Condensed Milk',
        N'Small', 60.00, 1),
    (N'Coffee', N'Spanish Latte', N'Espresso, Coconut & Condensed Milk',
        N'Medium', 65.00, 2),

    /* ---------- FREEZOS ---------- */

    (N'Freezos', N'Coffee Freezo', NULL, N'Medium', 65.00, 1),
    (N'Freezos', N'Coffee Freezo', NULL, N'Large', 70.00, 2),

    (
        N'Freezos', N'Salted Caramel Freezo',
        N'Espresso, Salted Caramel & Milk',
        N'Medium', 65.00, 1
    ),
    (
        N'Freezos', N'Salted Caramel Freezo',
        N'Espresso, Salted Caramel & Milk',
        N'Large', 70.00, 2
    ),

    (
        N'Freezos', N'S''Mores Freezo',
        N'Espresso, Toasted Marshmellow & Milk',
        N'Medium', 70.00, 1
    ),
    (
        N'Freezos', N'S''Mores Freezo',
        N'Espresso, Toasted Marshmellow & Milk',
        N'Large', 75.00, 2
    ),

    (
        N'Freezos', N'Skinner Freezo',
        N'Peach, Cream & Vanilla',
        N'Medium', 65.00, 1
    ),
    (
        N'Freezos', N'Skinner Freezo',
        N'Peach, Cream & Vanilla',
        N'Large', 75.00, 2
    ),

    /* ---------- ICED LATTES ---------- */

    (
        N'Iced Lattes', N'Iced Coffee',
        N'Long Coffee With Ice',
        N'Medium', 35.00, 1
    ),
    (
        N'Iced Lattes', N'Iced Coffee',
        N'Long Coffee With Ice',
        N'Large', 40.00, 2
    ),

    (
        N'Iced Lattes', N'Iced Caffe Latte',
        N'Espresso & Cold Milk',
        N'Medium', 40.00, 1
    ),
    (
        N'Iced Lattes', N'Iced Caffe Latte',
        N'Espresso & Cold Milk',
        N'Large', 45.00, 2
    ),

    (
        N'Iced Lattes', N'Iced Caffe Mocha',
        N'Espresso, Chocolate & Cream',
        N'Medium', 40.00, 1
    ),
    (
        N'Iced Lattes', N'Iced Caffe Mocha',
        N'Espresso, Chocolate & Cream',
        N'Large', 45.00, 2
    ),

    (
        N'Iced Lattes', N'Frosted Caramel Latte',
        N'Espresso, Caramel & Cream',
        N'Medium', 45.00, 1
    ),
    (
        N'Iced Lattes', N'Frosted Caramel Latte',
        N'Espresso, Caramel & Cream',
        N'Large', 50.00, 2
    ),

    /* ---------- TEA ---------- */

    (N'Tea', N'Rooibos/Earl Grey', NULL, N'Medium', 30.00, 1),
    (N'Tea', N'Rooibos/Earl Grey', NULL, N'Large', 32.00, 2),

    (N'Tea', N'Green/Chai/Peppermint', NULL, N'Medium', 35.00, 1),
    (N'Tea', N'Green/Chai/Peppermint', NULL, N'Large', 38.00, 2),

    /* ---------- SOFT DRINKS ---------- */

    (N'Soft Drinks', N'Mountain Falls Still 500ML',
        NULL, N'Standard', 35.00, 1),
    (N'Soft Drinks', N'Mountain Falls Sparkling 1L',
        NULL, N'Standard', 65.00, 1),
    (N'Soft Drinks', N'San Pellegrino Sodas',
        NULL, N'Standard', 55.00, 1),
    (N'Soft Drinks', N'Sir Fruit Fresh Juices',
        NULL, N'Standard', 50.00, 1),
    (N'Soft Drinks', N'Boosters',
        NULL, N'Standard', 45.00, 1),

    /* ---------- BREAKFAST ---------- */

    (
        N'Breakfast', N'Yoghurt & Muesli',
        N'Creamy Yoghurt, Crunchy Muesli & Fresh Berries',
        N'Standard', 65.00, 1
    ),
    (
        N'Breakfast', N'Toasted Cheese Bagel',
        N'Toasted Bagel, Melted Mozzarella & Cheddar Cheese',
        N'Standard', 65.00, 1
    ),
    (
        N'Breakfast', N'Ham & Cheese Croissant',
        N'Flaky Croissant, Savory Ham & Melted Cheese',
        N'Standard', 70.00, 1
    );


    /* ==========================================
       3. INSERT PRODUCTS

       Stock tracking remains disabled by default.
       We can enable it individually when inventory
       rules for each product are configured.
       ========================================== */

    INSERT INTO dbo.Products
    (
        CategoryID,
        ProductName,
        Description
    )
    SELECT DISTINCT
        c.CategoryID,
        m.ProductName,
        m.Description
    FROM @Menu m
    INNER JOIN dbo.Categories c
        ON c.CategoryName = m.CategoryName
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.Products p
        WHERE p.CategoryID = c.CategoryID
          AND p.ProductName = m.ProductName
    );


    /* ==========================================
       4. INSERT PRODUCT VARIANTS AND PRICES
       ========================================== */

    INSERT INTO dbo.ProductVariants
    (
        ProductID,
        VariantName,
        Price,
        DisplayOrder
    )
    SELECT
        p.ProductID,
        m.VariantName,
        m.Price,
        m.VariantOrder
    FROM @Menu m
    INNER JOIN dbo.Categories c
        ON c.CategoryName = m.CategoryName
    INNER JOIN dbo.Products p
        ON p.CategoryID = c.CategoryID
       AND p.ProductName = m.ProductName
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.ProductVariants v
        WHERE v.ProductID = p.ProductID
          AND v.VariantName = m.VariantName
    );


    /* ==========================================
       5. INSERT ADD-ONS

       Each option costs R8.
       Product eligibility will be configured later.
       ========================================== */

    INSERT INTO dbo.AddOns (AddOnName, Price)
    SELECT a.AddOnName, a.Price
    FROM
    (
        VALUES
            (N'Extra Syrup Shot', CONVERT(DECIMAL(10,2), 8.00)),
            (N'Oat Milk Alternative', CONVERT(DECIMAL(10,2), 8.00)),
            (N'Almond Milk Alternative', CONVERT(DECIMAL(10,2), 8.00)),
            (N'Macadamia Milk Alternative', CONVERT(DECIMAL(10,2), 8.00))
    ) AS a(AddOnName, Price)
    WHERE NOT EXISTS
    (
        SELECT 1
        FROM dbo.AddOns existing
        WHERE existing.AddOnName = a.AddOnName
    );


    COMMIT TRANSACTION;

    PRINT 'Skinner Cafe menu and add-ons seeded successfully.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO
