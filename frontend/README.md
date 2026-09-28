# Personal Finance — Frontend

A React client for the Personal Finance API. Built with Vite, React Router, and Recharts.

## Stack

- React 18 + Vite
- React Router (client-side routing)
- Recharts (category spending chart)
- Plain CSS with design tokens (light/dark theme via CSS variables)
- No CSS framework, no state management library — Context API for auth/theme/toasts

## Getting started

1. Install dependencies:
   ```bash
   npm install
   ```

2. Copy the environment file and point it at your running API:
   ```bash
   cp .env.example .env
   ```
   By default it expects the API at `http://localhost:8080`.

3. Make sure your Spring Boot API is running and has CORS enabled for this app's origin (see the `CorsConfig` note below).

4. Start the dev server:
   ```bash
   npm run dev
   ```
   Open the URL Vite prints (usually `http://localhost:5173`).

## Backend CORS requirement

This frontend runs on its own origin (`http://localhost:5173` by default), different from the API (`http://localhost:8080`). Your Spring Boot backend needs a CORS configuration allowing that origin, for example:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}
```

## Project structure

```
src/
  api/          fetch wrapper + one file per resource (auth, categories, transactions, dashboard)
  context/      Auth, Theme and Toast providers
  components/
    ui/         Button, Input, Select, Modal, Badge, Loading, EmptyState
    layout/     Sidebar, Topbar, MobileNav, AppLayout
  pages/        Login, Register, Dashboard, Transactions, Categories
  utils/        currency/date formatting (pt-BR currency, en-GB dates)
```

## Notes on scope

- The "Income vs Expenses over time" line chart from the spec was intentionally **not** implemented: the API has no endpoint returning monthly totals, and the spec explicitly asks not to fabricate data. The category breakdown chart (bar chart, using `/api/reports/categories`) covers the same instinct without inventing anything.
- All CRUD, filtering and pagination is backed by real API calls — nothing is mocked.
- JWT is stored in `localStorage` and attached to every authenticated request. A `401` response automatically logs the user out and redirects to `/login`.
