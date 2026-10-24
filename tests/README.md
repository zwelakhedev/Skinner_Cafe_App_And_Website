\# Skinner Cafe - Testing



\## Overview



This directory is reserved for automated tests covering the Skinner Cafe system.



Tests will help verify API behaviour, website functionality, and Android application behaviour as those components are implemented.



\## Directory Structure



\- `api/` - API unit tests and integration tests.

\- `web/` - Website component and application tests.

\- `android/` - Android unit tests and instrumentation tests.



Test project files may be placed in these directories as the corresponding testing frameworks are selected.



\## Current Status



The API project currently builds successfully. Dedicated Skinner Cafe test projects and test suites have not yet been established.



\## Testing Principles



\- Test expected behaviour, validation, and error handling.

\- Test authentication and authorisation for protected operations.

\- Test important business rules and database constraints.

\- Keep test data separate from production data.

\- Avoid storing credentials or other secrets in source control.

\- Make automated tests repeatable and suitable for continuous integration.



\## API Tests



API tests should cover HTTP responses, request validation, authorisation, business logic, and database interactions where applicable.



Integration tests should use an isolated test configuration and must not modify production data.



\## Website Tests



Website tests should cover important components, user interactions, validation, and API integration.



\## Android Tests



Android tests should cover important application logic, UI behaviour, and communication with the shared API.



\## Running Tests



Once test projects have been created, their setup and execution commands will be documented here. For .NET test projects, the general command is:



```powershell

dotnet test

```



Run it from the appropriate solution or test-project directory.
