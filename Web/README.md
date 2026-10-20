\# Skinner Cafe — Website



\## Overview



This directory contains the Skinner Cafe website, built with React, TypeScript, and Vite. The initial interface was created using Figma Make and integrated into the Skinner Cafe repository.



The website is intended to become one of the system's client applications, communicating with the shared ASP.NET Core Web API for backend functionality.



\## Technology Stack



\- React

\- TypeScript

\- Vite

\- Tailwind CSS

\- pnpm for package management



Refer to `package.json` for the project's exact dependency versions and available scripts.



\## Prerequisites



\- Node.js

\- pnpm

\- Git



\## Installation



From the repository root, navigate to the website directory:



```powershell

cd Web

pnpm install

```



\## Run the Development Server



```powershell

pnpm run dev

```



Vite will display the local URL in the terminal. Open that URL in your browser.



\## Production Build



```powershell

pnpm run build

```



Vite generates the production build in the `dist/` directory.



\## Project Structure



```text

Web/

├── src/

│   ├── app/

│   │   ├── App.tsx

│   │   └── components/

│   ├── imports/

│   ├── styles/

│   └── main.tsx

├── guidelines/

├── index.html

├── package.json

├── postcss.config.mjs

└── vite.config.ts

```



This is a high-level overview. Consult the actual source tree for the complete structure.



\## Development Guidelines



\- Keep website-specific components and styling inside `Web/`.

\- Keep API communication code separate from presentation components where practical.

\- Do not connect directly to SQL Server from the browser.

\- Store no passwords, private API keys, or other secrets in frontend code.

\- Validate important business rules on the backend, even when the website also validates input.



\## Backend Integration



The intended backend is located in `API/`. The website and Android app will use the same API.



API integration will be documented further when the relevant endpoints and authentication mechanisms are implemented.



\## Attributions



See `ATTRIBUTIONS.md` for website asset attributions and credits.
