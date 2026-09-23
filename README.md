# Personal Finance API

A REST API for personal income and expense tracking, built with Java and Spring Boot. Users can register, log in, create their own categories, record income and expense transactions, filter and paginate results, and view financial dashboards and category-based spending reports.

Built as a portfolio project to practice real-world backend development, including layered architecture, JWT authentication, business rule enforcement, data isolation, and automated testing.

## Features

* **Authentication:** user registration and login with JWT (Bearer token)
* **Categories:** full CRUD, scoped to the authenticated user
* **Transactions:** full CRUD with income/expense types, category association, payment method, and monetary values handled with `BigDecimal`
* **Filters:** transactions can be filtered by date range, type, and category
* **Pagination:** transaction listings are paginated
* **Dashboard:** total income, total expenses, and balance for a given period
* **Reports:** expenses grouped and summed by category
* **Data isolation:** user-owned resources are scoped to the authenticated user at the service and repository levels
* **API documentation:** interactive Swagger UI with JWT authentication support

## Tech Stack

| Category      | Technology                         |
| ------------- | ---------------------------------- |
| Language      | Java 21                            |
| Framework     | Spring Boot                        |
| Persistence   | Spring Data JPA / Hibernate        |
| Database      | MySQL                              |
| Test Database | H2                                 |
| Security      | Spring Security + JJWT             |
| Validation    | Jakarta Validation                 |
| Testing       | JUnit 5, Mockito, AssertJ, MockMvc |
| Documentation | springdoc-openapi / Swagger UI     |
| Build         | Maven                              |

## Architecture

The application follows a layered architecture:

```text
controller/   → receives HTTP requests and delegates to services
service/      → business logic and rule enforcement
repository/   → Spring Data JPA repositories and data access
entity/       → JPA entities
dto/
  request/    → input contracts
  response/   → output contracts
mapper/       → entity ↔ DTO conversion
exception/    → custom exceptions and global exception handler
security/     → JWT generation/validation, authentication filter, user context
config/       → application and security configuration
```

### Key Design Decisions

* Business rules are handled in the service layer rather than the controllers
* DTOs are used at API boundaries, keeping entities from being exposed directly
* Queries involving user-owned resources are scoped to the authenticated user's ID
* Monetary values use `BigDecimal` instead of `float` or `double`
* Ownership checks return `404 Not Found` when appropriate, avoiding unnecessary resource-existence disclosure

## Testing

The project includes tests at different levels:

* **Unit tests:** business logic in the service layer
* **Controller tests:** HTTP behavior and endpoint responses using MockMvc
* **Repository tests:** custom JPA queries using `@DataJpaTest`
* **Integration tests:** complete application flows across multiple layers

The goal is to test meaningful application behavior rather than simply maximize test count.

## Getting Started

### Prerequisites

* Java 21
* Maven, or use the included Maven Wrapper
* MySQL running locally

### 1. Create the database

```sql
CREATE DATABASE personal_finance_db;
```

### 2. Configure environment variables

The application reads database credentials and the JWT secret from environment variables:

| Variable      | Description                                                  |
| ------------- | ------------------------------------------------------------ |
| `DB_USERNAME` | MySQL username, defaults to `root`                           |
| `DB_PASSWORD` | MySQL password                                               |
| `JWT_SECRET`  | Secret key used to sign JWTs, must be at least 32 characters |

Configure these variables in your IDE's run configuration or export them in your shell before running the application.

### 3. Run the application

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```cmd
mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

### 4. Run the tests

```bash
./mvnw test
```

On Windows:

```cmd
mvnw.cmd test
```

Tests use an in-memory H2 database and do not require MySQL.

## API Documentation

Once the application is running, interactive API documentation is available through Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

To test protected endpoints:

1. Call `POST /api/auth/login` with valid credentials
2. Copy the returned JWT token
3. Click **Authorize** in Swagger UI
4. Enter the token using the `Bearer <token>` format
5. Protected endpoints can now be tested directly from Swagger UI

## Main Endpoints

| Method | Endpoint                   | Description                                     |
| ------ | -------------------------- | ----------------------------------------------- |
| POST   | `/api/auth/register`       | Register a new user                             |
| POST   | `/api/auth/login`          | Log in and receive a JWT                        |
| GET    | `/api/categories`          | List categories                                 |
| POST   | `/api/categories`          | Create a category                               |
| PUT    | `/api/categories/{id}`     | Update a category                               |
| DELETE | `/api/categories/{id}`     | Delete a category                               |
| GET    | `/api/transactions`        | List paginated transactions                     |
| POST   | `/api/transactions`        | Create a transaction                            |
| GET    | `/api/transactions/{id}`   | Get a single transaction                        |
| PUT    | `/api/transactions/{id}`   | Update a transaction                            |
| DELETE | `/api/transactions/{id}`   | Delete a transaction                            |
| GET    | `/api/transactions/filter` | Filter transactions by date, type, and category |
| GET    | `/api/dashboard`           | View income, expenses, and balance for a period |
| GET    | `/api/reports/categories`  | View expenses grouped by category               |

Full request and response contracts are available in Swagger UI.

## Example: Create a Transaction

```http
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "description": "Lunch",
  "amount": 32.90,
  "type": "EXPENSE",
  "categoryId": 3,
  "date": "2026-09-13",
  "paymentMethod": "CREDIT_CARD"
}
```

## Roadmap

Possible future improvements:

* Docker and Docker Compose for simplified setup
* Recurring transactions
* Budgets per category
* CSV/OFX import
* Multiple accounts or wallets
* Cash, checking account, and credit card support
