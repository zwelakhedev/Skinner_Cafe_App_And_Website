/*
    Skinner Cafe
    Script: 04_CreatePaymentsAndInventory.sql
    Purpose: Payments, inventory, stock deliveries, and stock movements.
*/

USE SkinnerCafe;
GO

/* 1. Payment attempts and payment records.
   Never store card numbers, CVVs, or other raw card credentials here.
*/
CREATE TABLE dbo.Payments
(
    PaymentID BIGINT IDENTITY(1,1) NOT NULL,
    OrderID INT NOT NULL,
    PaymentMethod NVARCHAR(20) NOT NULL,
    PaymentStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Payments_PaymentStatus DEFAULT ('Pending'),
    Amount DECIMAL(10,2) NOT NULL,
    ProviderReference NVARCHAR(200) NULL,
    IdempotencyKey NVARCHAR(100) NULL,
    CreatedAt DATETIME2(0) NOT NULL
        CONSTRAINT DF_Payments_CreatedAt DEFAULT (SYSUTCDATETIME()),
    PaidAt DATETIME2(0) NULL,

    CONSTRAINT PK_Payments PRIMARY KEY (PaymentID),
    CONSTRAINT FK_Payments_Orders
        FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID),
    CONSTRAINT CK_Payments_Method
        CHECK (PaymentMethod IN ('Card', 'Online')),
    CONSTRAINT CK_Payments_Status
        CHECK (PaymentStatus IN ('Pending', 'Successful', 'Failed', 'Refunded', 'PartiallyRefunded')),
    CONSTRAINT CK_Payments_Amount CHECK (Amount > 0)
);
GO

CREATE INDEX IX_Payments_OrderID_CreatedAt
    ON dbo.Payments(OrderID, CreatedAt DESC);
GO

/* Prevent a repeated idempotency key from creating duplicate attempts
   for the same order. NULL keys are allowed.
*/
CREATE UNIQUE INDEX UX_Payments_Order_IdempotencyKey
    ON dbo.Payments(OrderID, IdempotencyKey)
    WHERE IdempotencyKey IS NOT NULL;
GO

/* 2. Current stock quantity for products that are stock-tracked. */
CREATE TABLE dbo.ProductInventory
(
    ProductID INT NOT NULL,
    QuantityOnHand INT NOT NULL
        CONSTRAINT DF_ProductInventory_QuantityOnHand DEFAULT (0),
    LowStockThreshold INT NOT NULL
        CONSTRAINT DF_ProductInventory_LowStockThreshold DEFAULT (5),
    UpdatedAt DATETIME2(0) NOT NULL
        CONSTRAINT DF_ProductInventory_UpdatedAt DEFAULT (SYSUTCDATETIME()),

    CONSTRAINT PK_ProductInventory PRIMARY KEY (ProductID),
    CONSTRAINT FK_ProductInventory_Products
        FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID),
    CONSTRAINT CK_ProductInventory_Quantity CHECK (QuantityOnHand >= 0),
    CONSTRAINT CK_ProductInventory_LowStockThreshold CHECK (LowStockThreshold >= 0)
);
GO

/* 3. Stock deliveries received from suppliers. */
CREATE TABLE dbo.StockDeliveries
(
    StockDeliveryID INT IDENTITY(1,1) NOT NULL,
    SupplierName NVARCHAR(200) NOT NULL,
    DeliveryReference NVARCHAR(100) NULL,
    ReceivedAt DATETIME2(0) NOT NULL
        CONSTRAINT DF_StockDeliveries_ReceivedAt DEFAULT (SYSUTCDATETIME()),
    ReceivedByIdentityUserId NVARCHAR(450) NULL,
    Notes NVARCHAR(300) NULL,

    CONSTRAINT PK_StockDeliveries PRIMARY KEY (StockDeliveryID)
);
GO

/* 4. Individual products in each delivery. */
CREATE TABLE dbo.StockDeliveryItems
(
    StockDeliveryItemID INT IDENTITY(1,1) NOT NULL,
    StockDeliveryID INT NOT NULL,
    ProductID INT NOT NULL,
    QuantityReceived INT NOT NULL,

    CONSTRAINT PK_StockDeliveryItems PRIMARY KEY (StockDeliveryItemID),
    CONSTRAINT FK_StockDeliveryItems_StockDeliveries
        FOREIGN KEY (StockDeliveryID) REFERENCES dbo.StockDeliveries(StockDeliveryID),
    CONSTRAINT FK_StockDeliveryItems_Products
        FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID),
    CONSTRAINT CK_StockDeliveryItems_Quantity CHECK (QuantityReceived > 0)
);
GO

/* 5. Stock movement ledger.
   Sale quantities are negative; deliveries are positive.
   Adjustments and wastage are recorded as signed quantities.
*/
CREATE TABLE dbo.StockMovements
(
    StockMovementID BIGINT IDENTITY(1,1) NOT NULL,
    ProductID INT NOT NULL,
    MovementType NVARCHAR(20) NOT NULL,
    QuantityChange INT NOT NULL,
    OrderID INT NULL,
    StockDeliveryID INT NULL,
    StockDeliveryItemID INT NULL,
    ChangedByIdentityUserId NVARCHAR(450) NULL,
    Reason NVARCHAR(300) NULL,
    CreatedAt DATETIME2(0) NOT NULL
        CONSTRAINT DF_StockMovements_CreatedAt DEFAULT (SYSUTCDATETIME()),

    CONSTRAINT PK_StockMovements PRIMARY KEY (StockMovementID),
    CONSTRAINT FK_StockMovements_Products
        FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID),
    CONSTRAINT FK_StockMovements_Orders
        FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID),
    CONSTRAINT FK_StockMovements_StockDeliveries
        FOREIGN KEY (StockDeliveryID) REFERENCES dbo.StockDeliveries(StockDeliveryID),
    CONSTRAINT FK_StockMovements_StockDeliveryItems
        FOREIGN KEY (StockDeliveryItemID) REFERENCES dbo.StockDeliveryItems(StockDeliveryItemID),
    CONSTRAINT CK_StockMovements_Type
        CHECK (MovementType IN ('Sale', 'Delivery', 'Wastage', 'Adjustment')),
    CONSTRAINT CK_StockMovements_NonZero CHECK (QuantityChange <> 0),
    CONSTRAINT CK_StockMovements_Sale
        CHECK (MovementType <> 'Sale' OR (OrderID IS NOT NULL AND QuantityChange < 0)),
    CONSTRAINT CK_StockMovements_Delivery
        CHECK (MovementType <> 'Delivery' OR QuantityChange > 0)
);
GO

/* One sale movement per order and product.
   The API must aggregate each order's quantity per product and write
   the movement and stock deduction in the same database transaction.
*/
CREATE UNIQUE INDEX UX_StockMovements_Order_Product_Sale
    ON dbo.StockMovements(OrderID, ProductID)
    WHERE MovementType = 'Sale' AND OrderID IS NOT NULL;
GO

CREATE INDEX IX_StockMovements_ProductID_CreatedAt
    ON dbo.StockMovements(ProductID, CreatedAt DESC);
GO

PRINT 'Payment and inventory tables created successfully.';
GO
