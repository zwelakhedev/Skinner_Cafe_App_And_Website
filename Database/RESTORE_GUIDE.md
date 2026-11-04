# Skinner Cafe Database Restore Guide

## 1. Purpose

This guide explains how to restore the supplied `SkinnerCafe_Assessment.bak` database backup using Microsoft SQL Server Management Studio (SSMS).

The backup was created from **SkinnerCafe** on **Microsoft SQL Server 2019 Express (64-bit), version 15.0.4382.1**, on 4 November 2026.

The backup was checked with SQL Server `RESTORE VERIFYONLY WITH CHECKSUM`, which reported:

> The backup set on file 1 is valid.

## 2. Requirements

Before restoring the database, install:

- Microsoft SQL Server 2019 or a newer compatible SQL Server version.
- Microsoft SQL Server Management Studio (SSMS).

A backup created by SQL Server 2019 can be restored to SQL Server 2019 or newer. SQL Server backups cannot be restored to an older major SQL Server version.

The person restoring the database needs permission to create and restore databases on the SQL Server instance.

## 3. Restore the database using SSMS

1. Copy `SkinnerCafe_Assessment.bak` to a folder accessible to the SQL Server instance.
2. Open SQL Server Management Studio.
3. Connect to the SQL Server instance.
4. In Object Explorer, right-click **Databases**.
5. Select **Restore Database...**.
6. Under **Source**, select **Device**.
7. Select the browse (`...`) button.
8. In the Select backup devices window, choose **Add**.
9. Browse to `SkinnerCafe_Assessment.bak`, select it, and confirm.
10. Confirm that the `SkinnerCafe` backup set appears in the restore window.
11. Under **Destination**, set the database name to `SkinnerCafe`.
12. Open the **Files** page and check that the data and log file destination paths are valid for the SQL Server instance. Change them if necessary.
13. Open the **Options** page. If a database named `SkinnerCafe` already exists and must be replaced, only select **Overwrite the existing database (WITH REPLACE)** when you are certain that replacing it is intended and you have protected any existing data.
14. Select **OK** to start the restore.
15. Wait for SSMS to report that the database was restored successfully.

**Important:** Do not overwrite an existing database unless you have permission and understand that its current contents may be replaced.

## 4. Verify the restored database

In SSMS, expand:

**Databases → SkinnerCafe → Tables**

The restored database should contain the business tables, ASP.NET Core Identity tables, and the EF Core migrations history table.

To check the database name and table count, open a new query window against the restored instance and run:

```sql
USE [SkinnerCafe];
GO

SELECT DB_NAME() AS DatabaseName;

SELECT COUNT(*) AS TotalTables
FROM sys.tables;

SELECT COUNT(*) AS TotalForeignKeys
FROM sys.foreign_keys;
```

The supplied database snapshot contained:

- **23 tables**
- **24 foreign-key constraints**

The database also includes the seeded menu data and test records present at the time the backup was created.

## 5. Supporting repository files

The GitHub repository contains the database documentation and SQL scripts:

- `Database/README.md` — database overview.
- `Database/schema/README.md` — schema and relationship documentation.
- `Database/scripts/` — SQL scripts for database creation and initial data.
- `Database/schema/SkinnerCafe_ERD.pdf` — printable entity relationship diagram, if included in the submitted repository.

The `.bak` file is supplied separately as an assessment artifact. It is not required to be committed to the public GitHub repository.

## 6. Security and assessment notes

- The backup contains the database records as they existed when it was created, including ASP.NET Identity account records and password hashes. Passwords are not stored as plain text.
- Treat the backup as confidential assessment material. Do not publish it in a public repository.
- The backup does not contain the running API, website, Android application, or SQL Server installation. It contains the database and its data.
- The database backup is a snapshot. Changes made to the live development database after 4 November 2026 will not automatically appear in this backup.
- Use test accounts and test data when demonstrating the application.

---
**Project:** Skinner Cafe  
**Database:** SkinnerCafe  
**Backup file:** `SkinnerCafe_Assessment.bak`  
**Backup platform:** Microsoft SQL Server 2019 Express (64-bit)  
