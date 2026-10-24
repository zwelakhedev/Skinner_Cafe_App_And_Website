/*
    Skinner Cafe
    Script: 01_CreateDatabase.sql
    Purpose: Create the development database if it does not exist.
*/

USE master;
GO

IF DB_ID(N'SkinnerCafe') IS NULL
BEGIN
    CREATE DATABASE SkinnerCafe;
    PRINT 'SkinnerCafe database created successfully.';
END
ELSE
BEGIN
    PRINT 'SkinnerCafe database already exists. No changes made.';
END;
GO
