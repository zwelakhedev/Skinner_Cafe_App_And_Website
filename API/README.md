\# Skinner Cafe - API



\## Overview



This directory contains the shared ASP.NET Core Web API for the Skinner Cafe system.



The API is intended to serve both the React website in `Web/` and the Kotlin Android application planned for `Android/`.



The backend will centralise business rules, authentication, authorisation, validation, and database access.



\## Current Status



The initial controller-based ASP.NET Core Web API project has been created and successfully built.



Current foundations include:



\- ASP.NET Core Web API targeting .NET 10

\- Controller support

\- OpenAPI document generation in development

\- Initial backend folder structure



Database integration, authentication, and cafe management endpoints have not yet been implemented.



\## Technology Stack



\- C#

\- ASP.NET Core Web API

\- .NET 10

\- OpenAPI



\## Project Structure



```text

API/

├── SkinnerCafe.slnx

├── README.md

└── SkinnerCafe.API/

&#x20;   ├── Controllers/

&#x20;   ├── Data/

&#x20;   ├── DTOs/

&#x20;   ├── Middleware/

&#x20;   ├── Models/

&#x20;   ├── Properties/

&#x20;   ├── Services/

&#x20;   ├── Program.cs

&#x20;   ├── appsettings.json

&#x20;   └── SkinnerCafe.API.csproj

```



\## Prerequisites



\- A compatible .NET SDK

\- A code editor or IDE

\- SQL Server will be required when database integration is introduced



\## Build the API



From the repository root:



```powershell

cd API

dotnet build SkinnerCafe.slnx

```



\## Run the API



From the `API/` directory:



```powershell

dotnet run --project SkinnerCafe.API/SkinnerCafe.API.csproj

```



The terminal will display the local addresses on which the API is listening.



\## OpenAPI



In the development environment, the application maps an OpenAPI document endpoint. An interactive Swagger UI has not yet been configured.



\## Planned Backend Responsibilities



\- User authentication and role-based authorisation

\- Point of sale and order management

\- Inventory and stock control

\- Staff management

\- Payment provider integration

\- Reporting and analytics

\- Event management

\- SQL Server data access



These features will be added incrementally.



\## Security



\- Never commit passwords, connection-string credentials, signing keys, or other secrets.

\- Validate requests on the server.

\- Apply authorisation policies to protected endpoints.

\- Keep database access inside the backend rather than exposing SQL Server to client applications.
