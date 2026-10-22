
/*
    Skinner Cafe
    Script: 03_CreateAccountsAndOrders.sql
    Purpose: Create customer profiles, orders, order items,
             add-on selections, and order status history.

    ASP.NET Core Identity will manage authentication and staff roles.
*/

USE SkinnerCafe;
GO

/* 1. Customer profile.
   Authentication credentials will be managed by ASP.NET Core Identity.
   IdentityUserId will link to the Identity user after Identity is configured.
*/
CREATE TABLE dbo.CustomerProfiles
(
    CustomerID INT IDENTITY(1,1) NOT NULL,
    IdentityUserId NVARCHAR(450) NOT NULL,
    FirstName NVARCHAR(100) NOT NULL,
    LastName NVARCHAR(100) NOT NULL,
    Email NVARCHAR(256) NOT NULL,
    PhoneNumber NVARCHAR(30) NULL,
    CreatedAt DATETIME2(0) NOT NULL
        CONSTRAINT DF_CustomerProfiles_CreatedAt DEFAULT (SYSUTCDATETIME()),
    IsActive BIT NOT NULL
        CONSTRAINT DF_CustomerProfiles_IsActive DEFAULT (1),

    CONSTRAINT PK_CustomerProfiles PRIMARY KEY (CustomerID),
    CONSTRAINT UQ_CustomerProfiles_IdentityUserId UNIQUE (IdentityUserId),
    CONSTRAINT UQ_CustomerProfiles_Email UNIQUE (Email)
);
GO

/* 2. Orders.
   CustomerID is nullable to support guest checkout.
   Contact details are snapshots for collection and order history.
*/
CREATE TABLE dbo.Orders
(
    OrderID INT IDENTITY(1,1) NOT NULL,
    OrderNumber NVARCHAR(30) NOT NULL,
    CustomerID INT NULL,
    OrderSource NVARCHAR(10) NOT NULL,
    OrderStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Orders_OrderStatus DEFAULT ('Pending'),
    PaymentStatus NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Orders_PaymentStatus DEFAULT ('Pending'),
    CustomerName NVARCHAR(200) NOT NULL,
    CustomerEmail NVARCHAR(256) NULL,
    CustomerPhone NVARCHAR(30) NULL,
    Subtotal DECIMAL(10,2) NOT NULL,
    DiscountAmount DECIMAL(10,2) NOT NULL
        CONSTRAINT DF_Orders_DiscountAmount DEFAULT (0),
    TotalAmount DECIMAL(10,2) NOT NULL,
    CreatedAt DATETIME2(0) NOT NULL
        CONSTRAINT DF_Orders_CreatedAt DEFAULT (SYSUTCDATETIME()),
    CompletedAt DATETIME2(0) NULL,

    CONSTRAINT PK_Orders PRIMARY KEY (OrderID),
    CONSTRAINT UQ_Orders_OrderNumber UNIQUE (OrderNumber),
    CONSTRAINT FK_Orders_CustomerProfiles
        FOREIGN KEY (CustomerID) REFERENCES dbo.CustomerProfiles(CustomerID),
    CONSTRAINT CK_Orders_OrderSource
        CHECK (OrderSource IN ('POS', 'Online')),
    CONSTRAINT CK_Orders_OrderStatus
        CHECK (OrderStatus IN
            ('Pending', 'Confirmed', 'Preparing', 'Ready', 'Collected', 'Completed', 'Cancelled')),
    CONSTRAINT CK_Orders_PaymentStatus
        CHECK (PaymentStatus IN
            ('Pending', 'Successful', 'Failed', 'Refunded', 'PartiallyRefunded')),
    CONSTRAINT CK_Orders_Amounts
        CHECK (
            Subtotal >= 0
            AND DiscountAmount >= 0
            AND DiscountAmount <= Subtotal
            AND TotalAmount = Subtotal - DiscountAmount
        )
);
GO

/* 3. Order items.
   Product and variant names/prices are preserved as sale-time snapshots.
*/
CREATE TABLE dbo.OrderItems
(
    OrderItemID INT IDENTITY(1,1) NOT NULL,
    OrderID INT NOT NULL,
    VariantID INT NOT NULL,
    ProductNameSnapshot NVARCHAR(150) NOT NULL,
    VariantNameSnapshot NVARCHAR(50) NOT NULL,
    Quantity INT NOT NULL,
    UnitPrice DECIMAL(10,2) NOT NULL,
    LineTotal DECIMAL(10,2) NOT NULL,

    CONSTRAINT PK_OrderItems PRIMARY KEY (OrderItemID),
    CONSTRAINT FK_OrderItems_Orders
        FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID),
    CONSTRAINT FK_OrderItems_ProductVariants
        FOREIGN KEY (VariantID) REFERENCES dbo.ProductVariants(VariantID),
    CONSTRAINT CK_OrderItems_Quantity CHECK (Quantity > 0),
    CONSTRAINT CK_OrderItems_UnitPrice CHECK (UnitPrice >= 0),
    CONSTRAINT CK_OrderItems_LineTotal
        CHECK (LineTotal = Quantity * UnitPrice)
);
GO

/* 4. Add-ons selected for individual order items.
   The price snapshot preserves what the customer paid at the time.
*/
CREATE TABLE dbo.OrderItemAddOns
(
    OrderItemAddOnID INT IDENTITY(1,1) NOT NULL,
    OrderItemID INT NOT NULL,
    AddOnID INT NOT NULL,
    AddOnNameSnapshot NVARCHAR(100) NOT NULL,
    Quantity INT NOT NULL
        CONSTRAINT DF_OrderItemAddOns_Quantity DEFAULT (1),
    UnitPrice DECIMAL(10,2) NOT NULL,

    CONSTRAINT PK_OrderItemAddOns PRIMARY KEY (OrderItemAddOnID),
    CONSTRAINT FK_OrderItemAddOns_OrderItems
        FOREIGN KEY (OrderItemID) REFERENCES dbo.OrderItems(OrderItemID),
    CONSTRAINT FK_OrderItemAddOns_AddOns
        FOREIGN KEY (AddOnID) REFERENCES dbo.AddOns(AddOnID),
    CONSTRAINT CK_OrderItemAddOns_Quantity CHECK (Quantity > 0),
    CONSTRAINT CK_OrderItemAddOns_UnitPrice CHECK (UnitPrice >= 0)
);
GO

/* 5. Order status history for traceability. */
CREATE TABLE dbo.OrderStatusHistory
(
    OrderStatusHistoryID BIGINT IDENTITY(1,1) NOT NULL,
    OrderID INT NOT NULL,
    PreviousStatus NVARCHAR(20) NULL,
    NewStatus NVARCHAR(20) NOT NULL,
    ChangedByIdentityUserId NVARCHAR(450) NULL,
    Notes NVARCHAR(300) NULL,
    ChangedAt DATETIME2(0) NOT NULL
        CONSTRAINT DF_OrderStatusHistory_ChangedAt DEFAULT (SYSUTCDATETIME()),

    CONSTRAINT PK_OrderStatusHistory PRIMARY KEY (OrderStatusHistoryID),
    CONSTRAINT FK_OrderStatusHistory_Orders
        FOREIGN KEY (OrderID) REFERENCES dbo.Orders(OrderID),
    CONSTRAINT CK_OrderStatusHistory_NewStatus
        CHECK (NewStatus IN
            ('Pending', 'Confirmed', 'Preparing', 'Ready', 'Collected', 'Completed', 'Cancelled'))
);
GO

/* Indexes for common order lookups. */
CREATE INDEX IX_Orders_CustomerID_CreatedAt
    ON dbo.Orders(CustomerID, CreatedAt DESC);
GO

CREATE INDEX IX_Orders_OrderStatus_CreatedAt
    ON dbo.Orders(OrderStatus, CreatedAt DESC);
GO

CREATE INDEX IX_OrderItems_OrderID
    ON dbo.OrderItems(OrderID);
GO

CREATE INDEX IX_OrderStatusHistory_OrderID_ChangedAt
    ON dbo.OrderStatusHistory(OrderID, ChangedAt DESC);
GO

PRINT 'Customer, order, and order-history tables created successfully.';
GO
