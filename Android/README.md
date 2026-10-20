\# Skinner Cafe - Android Application



\## Overview



This directory is reserved for the Skinner Cafe Android application.



The planned application will be developed using Kotlin and Android development tools. It will communicate with the shared ASP.NET Core Web API in `API/` rather than connecting directly to SQL Server.



\## Current Status



The Android project has not yet been generated. This README documents the intended setup and architecture.



\## Planned Technology Stack



\- Kotlin

\- Android Studio

\- Android SDK

\- Jetpack Compose for the user interface, subject to final project setup

\- HTTP client for communicating with the shared API

\- Secure handling of authentication tokens and session state



The exact libraries and versions will be documented after the Android project is created.



\## Planned Responsibilities



Depending on the final requirements, the application may support:



\- Staff authentication

\- Point of sale and order management

\- Inventory operations for authorised staff

\- Event information and management

\- Sales summaries for authorised users



Feature access will depend on the user's role and the permissions enforced by the API.



\## Architecture



```text id="d0z42f"

Skinner Cafe Android App

&#x20;         |

&#x20;         | HTTPS requests

&#x20;         v

ASP.NET Core Web API

&#x20;         |

&#x20;         v

SQL Server Database

```



The API will be the central point for business rules, authorisation, and database access.



\## Development Setup



Once the Android project has been generated:



1\. Install Android Studio and a compatible Android SDK.

2\. Open the Android project in Android Studio.

3\. Configure the development API base URL.

4\. Run the application on an emulator or supported Android device.

5\. Test API communication against the shared backend.



\## Configuration and Security



\- Do not hardcode production credentials or secrets in the app.

\- Use HTTPS for API communication.

\- Do not rely on client-side role checks as the only security control.

\- Keep sensitive tokens out of source control and ordinary log output.

\- Use an emulator-specific API address when connecting to a backend running on the development computer.



\## Backend Integration



The shared backend is located in `API/`. Endpoint details and authentication requirements will be documented as those features are implemented.
