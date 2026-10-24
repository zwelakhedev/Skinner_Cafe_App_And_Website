# Skinner Cafe - Database

## Overview

This directory contains the database design and scripts for the Skinner Cafe system.

The planned database uses Microsoft SQL Server. It will be accessed by the ASP.NET Core Web API in `API/`. The React website and Android application must not connect directly to SQL Server.

## Current Status

The directory structure exists, but the database schema, connection configuration, and seed data have not yet been implemented.

## Directory Structure

- `schema/` - Database schema definitions and design documents.
- `scripts/` - SQL scripts for setup, upgrades, and repeatable operations.
- `seed-data/` - Safe development and test data.

## Planned Data Areas

Depending on the final requirements, the database may contain records for:

- Users, roles, and staff
- Products, categories, and inventory
- Orders and order items
- Payment transaction references
- Cafe events
- Sales reporting
- Optional customer loyalty functionality

The final schema will be defined before implementation.

## Security

- Keep database credentials out of source control.
- Use least-privilege database permissions.
- Never store real payment card details unnecessarily.
- Route application database operations through the API.
- Use migrations or reviewed SQL scripts to track schema changes.

## Development Workflow

1. Define data requirements and relationships.
2. Design and review the schema.
3. Implement the schema or Entity Framework Core migrations.
4. Configure the API database connection securely.
5. Test relationships, constraints, and data access.
6. Document database setup and update procedures.
