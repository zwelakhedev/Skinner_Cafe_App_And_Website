/*
    Skinner Cafe
    Script: 06_AddOrderCreationIdempotency.sql
    Purpose:
        Add order-creation idempotency support.

    Safe to execute more than once.
*/

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    -- Add the order idempotency key.
    IF COL_LENGTH('dbo.Orders', 'IdempotencyKey') IS NULL
    BEGIN
        ALTER TABLE dbo.Orders
        ADD IdempotencyKey NVARCHAR(36) NULL;
    END;

    -- Add the request fingerprint.
    IF COL_LENGTH('dbo.Orders', 'RequestFingerprint') IS NULL
    BEGIN
        ALTER TABLE dbo.Orders
        ADD RequestFingerprint NVARCHAR(64) NULL;
    END;

    -- Add the protected guest access token.
    IF COL_LENGTH('dbo.Orders', 'GuestAccessTokenProtected') IS NULL
    BEGIN
        ALTER TABLE dbo.Orders
        ADD GuestAccessTokenProtected NVARCHAR(MAX) NULL;
    END;

        -- Enforce uniqueness for non-null order idempotency keys.
    IF NOT EXISTS
    (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_Orders_IdempotencyKey'
          AND object_id = OBJECT_ID('dbo.Orders')
    )
    BEGIN
        EXEC sys.sp_executesql N'
            CREATE UNIQUE NONCLUSTERED INDEX UX_Orders_IdempotencyKey
            ON dbo.Orders (IdempotencyKey)
            WHERE IdempotencyKey IS NOT NULL;
        ';
    END;

    COMMIT TRANSACTION;

    PRINT 'Order creation idempotency schema updated successfully.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;