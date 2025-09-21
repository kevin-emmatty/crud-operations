# Product CRUD (Quarkus RESTEasy Reactive + CSV store)

## Overview
A small Product Management API built with Quarkus RESTEasy Reactive. It exposes CRUD and utility endpoints and persists data to a CSV file.

- Entity: Product { id, name, description, price, quantity }
- Storage: CSV file at `app.csv.path`
- JSON: Jackson (quarkus-rest-jackson)
- Error handling: Global JAX-RS ExceptionMapper with structured ErrorResponse
- Tests: QuarkusTest + REST-assured

## Stack
- Quarkus 3.x (RESTEasy Reactive)
- Lombok (DTOs/utility)
- OpenCSV (CSV I/O)

## Run
- Dev: `mvnw.cmd quarkus:dev` (Windows) or `./mvnw quarkus:dev`
- Build: `mvnw.cmd clean install`

If Windows blocks `target` cleanup, ensure no dev process holds the dev jar (stop quarkus:dev) and delete `target`.

## Configuration
- Main: `src/main/resources/application.properties`
  - `app.csv.path=data/products.csv`
- Test: `src/test/resources/application.properties`
  - `app.csv.path=target/test-products.csv`

## API Docs
- Swagger UI: http://localhost:8080/q/swagger-ui/

## Endpoints
Base path: `/products`

- POST `/products`
  - Body: array of Product objects (id, name, price, quantity required)
  - Creates non-duplicate ids; returns 201 Created (or 409 if all were duplicates)
  - Response: `{ summary: {created, duplicates, updated, total}, items: [...] }`

- GET `/products`
  - Query: optional `id`
  - No id: returns all products
  - With id: returns a single product (404 if not found)

- PUT `/products/{id}`
  - Body: single Product (partial or full). id in body must match path if provided
  - Patch/merge semantics over existing record (creates if not present)
  - Response: `{ summary: {created, updated, duplicates, total}, item: {...} }`

- DELETE `/products/{id}`
  - 204 No Content on success; 404 if missing

- GET `/products/{id}/availability?count=N`
  - Returns `{ id, requested, available, availableQuantity }`

- GET `/products/sorted/price?order=ASC|DESC`
  - Returns products sorted by price (ascending by default)

## DTOs
- `api.dto.ProductResponse`
- `api.dto.CreateProductsResponse`
- `api.dto.UpsertProductResponse`
- `api.dto.AvailabilityResponse`
- `dto.SummaryDto`

Mappers: `mapper.ProductMapper` (domain → DTO).

## Error Handling
- Throw `BadRequestException` or `NotFoundException` in controllers/services
- `api/exception/GlobalExceptionMapper` serializes errors:
  ```json
  {
    "status": 400,
    "error": "Bad Request",
    "message": "...",
    "path": "/products",
    "timestamp": "2025-09-19T15:00:00Z"
  }
  ```

## Tests
- `src/test/java/org/quarkus/assignment/api/ProductsControllerTest.java`
  - Covers create/list, get by id, put (merge), delete, availability, sorting
- Run: `mvnw.cmd test`

## Notes on Reactive
- This project uses RESTEasy Reactive APIs. CSV I/O is blocking; for fully reactive persistence, prefer a reactive database client (e.g., PostgreSQL reactive). If you keep CSV and anticipate load, mark blocking methods or offload to workers.

## Project Structure
- `api/` controllers
- `service/` business logic
- `util/` CSV I/O, URI constants
- `api/dto/`, `dto/` response DTOs
- `mapper/` domain→DTO mapping

## Coverage
- Generate report: `mvnw.cmd test` (Windows) or `./mvnw test`
- HTML report: `target/site/jacoco/index.html`
- Exec file: `target/jacoco.exec`
- Enforce thresholds (optional): run `mvnw.cmd verify` to fail build if coverage rules are added later
