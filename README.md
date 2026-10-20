# Skinner Cafe — App and Website

A multi-platform cafe management system consisting of a React website, a planned Kotlin Android application, a shared ASP.NET Core Web API, and a SQL Server database.

The project builds on the original Skinner App Cafe interface created with Figma Make.

**Original design:** [Skinner App Cafe on Figma](https://www.figma.com/design/j9ImuuhDhZkxuSXHGMVT3b/Skinner-App-Cafe)

## Project Status

The React website has been integrated into this repository. The shared API project and initial repository architecture are being established.

The Android application, database integration, and business features are planned components and should not be considered implemented until their development is complete.

## System Architecture

Both client applications will communicate with the same backend API. The API will handle business rules, authentication, authorisation, and database operations.

```text
React Website ───────┐
                     ├── ASP.NET Core Web API ─── SQL Server
Kotlin Android App ──┘
```

Clients should access backend functionality through the API rather than connecting directly to the database.

## Repository Structure

| Directory | Responsibility |
|---|---|
| `Web/` | React website built with Vite |
| `API/` | ASP.NET Core Web API and solution |
| `Android/` | Kotlin Android application |
| `Database/` | SQL Server schema, scripts, and seed data |
| `tests/` | Automated tests for project components |
| `docs/` | Requirements, design documents, diagrams, and reports |
| `.github/workflows/` | Continuous integration and automation |

See the README in each directory for component-specific information.

## Planned Features

- Point of sale (POS) and order management
- Inventory and stock management
- Staff and role management
- Payment processing
- Sales reporting and analytics
- Cafe events, DJs, and community activities
- Optional customer loyalty functionality

These features will be implemented incrementally.

## Technology Stack

| Component | Technology |
|---|---|
| Website | React, TypeScript, Vite |
| Android application | Kotlin and Android development tools |
| Backend | ASP.NET Core Web API with C# |
| Database | Microsoft SQL Server |
| Source control | Git and GitHub |
| API documentation | OpenAPI |

## Prerequisites

Install the tools required for the component you intend to work on:

- Git
- Node.js and pnpm for the website workspace
- .NET SDK compatible with the API target framework
- SQL Server and a database management tool when database integration begins
- Android Studio when Android development begins

## Running the Website

Open a terminal in the repository root and run:

```powershell
cd Web
pnpm install
pnpm run dev
```

Use the local URL printed by Vite to open the website.

## Building the API

Open a terminal in the repository root and run:

```powershell
cd API
dotnet build SkinnerCafe.slnx
```

The API currently represents the initial backend foundation. Database integration, authentication, and business endpoints will be added during subsequent development.

## Security Principles

- Keep passwords and secrets out of source control.
- Store environment-specific settings outside committed configuration when they contain sensitive values.
- Enforce authentication and role-based authorisation on protected API endpoints.
- Validate incoming data on the server.
- Never trust the client application to enforce security or business rules.

## Development Approach

The project is being developed incrementally. Each component will have its own documentation, and changes should be tested before being merged into the main branch.

## License and Attributions

The original interface was created with Figma Make. Refer to `Web/ATTRIBUTIONS.md` for website-specific asset attributions.

A project licence has not yet been specified.
