# API Testing Guide - Quote REST API

## Base URL

```
http://localhost:8080/api/v1
```

## Author Endpoints

### 1. Create Author

```http
POST /api/v1/authors
Content-Type: application/json

{
  "name": "Socrates",
  "biography": "Ancient Greek philosopher who founded Western philosophy",
  "birthYear": -469,
  "deathYear": -399
}
```

**Response:** 201 Created

```json
{
  "id": 1,
  "name": "Socrates",
  "biography": "Ancient Greek philosopher who founded Western philosophy",
  "birthYear": -469,
  "deathYear": -399,
  "createdAt": "2026-02-23T16:30:00",
  "updatedAt": "2026-02-23T16:30:00"
}
```

### 2. Get All Authors (Paginated)

```http
GET /api/v1/authors?page=0&size=10&sortBy=name&direction=ASC
```

**Response:** 200 OK

```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

### 3. Get Author by ID

```http
GET /api/v1/authors/1
```

**Response:** 200 OK (includes quotes)

### 4. Search Authors by Name

```http
GET /api/v1/authors/search?name=Socrat&page=0&size=10
```

### 5. Filter Authors by Birth Year

```http
GET /api/v1/authors/filter?birthYear=-469
```

### 6. Filter Authors by Year Range

```http
GET /api/v1/authors/filter?minYear=-500&maxYear=-400
```

### 7. Get Author's Quotes

```http
GET /api/v1/authors/1/quotes?page=0&size=10
```

### 8. Get Author Statistics

```http
GET /api/v1/authors/1/stats
```

**Response:** 200 OK

```json
{
  "authorId": 1,
  "authorName": "Socrates",
  "quoteCount": 42
}
```

### 9. Update Author (Full)

```http
PUT /api/v1/authors/1
Content-Type: application/json

{
  "name": "Socrates",
  "biography": "Updated biography",
  "birthYear": -469,
  "deathYear": -399
}
```

### 10. Partial Update Author

```http
PATCH /api/v1/authors/1
Content-Type: application/json

{
  "biography": "Updated biography only"
}
```

### 11. Delete Author

```http
DELETE /api/v1/authors/1
```

**Response:** 204 No Content

---

## Quote Endpoints

### 1. Create Quote

```http
POST /api/v1/quotes
Content-Type: application/json

{
  "text": "The unexamined life is not worth living.",
  "context": "At his trial",
  "category": "Philosophy",
  "authorId": 1
}
```

**Response:** 201 Created

```json
{
  "id": 1,
  "text": "The unexamined life is not worth living.",
  "context": "At his trial",
  "category": "Philosophy",
  "authorId": 1,
  "authorName": "Socrates",
  "createdAt": "2026-02-23T16:30:00",
  "updatedAt": "2026-02-23T16:30:00"
}
```

### 2. Get All Quotes (Paginated)

```http
GET /api/v1/quotes?page=0&size=10&sortBy=createdAt&direction=DESC
```

### 3. Get Quote by ID

```http
GET /api/v1/quotes/1
```

### 4. Search Quotes (General)

```http
GET /api/v1/quotes/search?q=life&page=0&size=10
```

Searches in both quote text and author name.

### 5. Search Quotes by Text Only

```http
GET /api/v1/quotes/search?text=unexamined
```

### 6. Search Quotes by Author Name

```http
GET /api/v1/quotes/search?author=Socrates
```

### 7. Filter Quotes (Multi-Criteria)

```http
GET /api/v1/quotes/filter?authorId=1&category=Philosophy&searchTerm=life
```

All filters are optional and can be used independently or combined.

### 8. Get All Categories

```http
GET /api/v1/quotes/categories
```

**Response:** 200 OK

```json
[
  "Philosophy",
  "Politics",
  "Science",
  "Literature"
]
```

### 9. Update Quote (Full)

```http
PUT /api/v1/quotes/1
Content-Type: application/json

{
  "text": "Updated quote text",
  "context": "Updated context",
  "category": "Philosophy",
  "authorId": 1
}
```

### 10. Partial Update Quote

```http
PATCH /api/v1/quotes/1
Content-Type: application/json

{
  "category": "Wisdom"
}
```

### 11. Delete Quote

```http
DELETE /api/v1/quotes/1
```

**Response:** 204 No Content

---

## Error Responses

### 404 Not Found

```json
{
  "timestamp": "2026-02-23T16:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Author not found with id: 999",
  "path": "/api/v1/authors/999"
}
```

### 400 Validation Error

```json
{
  "timestamp": "2026-02-23T16:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/authors",
  "validationErrors": {
    "name": "Author name is required",
    "birthYear": "Birth year must be at least -500"
  }
}
```

### 409 Conflict

```json
{
  "timestamp": "2026-02-23T16:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Author already exists with name: Socrates",
  "path": "/api/v1/authors"
}
```

---

## Common Query Parameters

### Pagination

- `page` - Page number (0-based, default: 0)
- `size` - Page size (1-100, default: 10)
- `sortBy` - Sort field (default: varies by endpoint)
- `direction` - Sort direction (ASC/DESC, default: varies by endpoint)

### Validation Constraints

#### Author:

- `name` - Required, 2-100 characters
- `biography` - Optional, max 1000 characters
- `birthYear` - Optional, -500 to 2100
- `deathYear` - Optional, -500 to 2100

#### Quote:

- `text` - Required, 10-2000 characters
- `context` - Optional, max 500 characters
- `category` - Optional, max 100 characters
- `authorId` - Required, must be positive and exist

---

## cURL Examples

### Create an Author

```bash
curl -X POST http://localhost:8080/api/v1/authors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Plato",
    "biography": "Student of Socrates",
    "birthYear": -428,
    "deathYear": -348
  }'
```

### Create a Quote

```bash
curl -X POST http://localhost:8080/api/v1/quotes \
  -H "Content-Type: application/json" \
  -d '{
    "text": "The only true wisdom is in knowing you know nothing.",
    "category": "Philosophy",
    "authorId": 1
  }'
```

### Get All Quotes (Paginated)

```bash
curl "http://localhost:8080/api/v1/quotes?page=0&size=5&sortBy=createdAt&direction=DESC"
```

### Search Quotes

```bash
curl "http://localhost:8080/api/v1/quotes/search?q=wisdom"
```

### Filter Quotes

```bash
curl "http://localhost:8080/api/v1/quotes/filter?authorId=1&category=Philosophy"
```

### Update Author

```bash
curl -X PUT http://localhost:8080/api/v1/authors/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Socrates",
    "biography": "Updated biography"
  }'
```

### Delete Quote

```bash
curl -X DELETE http://localhost:8080/api/v1/quotes/1
```

---

## PowerShell Examples (for Windows)

### Create an Author

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/authors" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{
    "name": "Plato",
    "biography": "Student of Socrates",
    "birthYear": -428,
    "deathYear": -348
  }'
```

### Get All Authors

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/authors?page=0&size=10" | Select-Object -ExpandProperty Content | ConvertFrom-Json
```

### Search Quotes

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/quotes/search?q=wisdom" | Select-Object -ExpandProperty Content | ConvertFrom-Json
```

---

## Swagger UI

The API documentation is available via Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

You can:
- View all endpoints
- Test endpoints directly from the browser
- See request/response schemas
- View validation rules
- Try out the API interactively

---

## Testing Checklist

### Author Operations

- [ ] Create author with valid data
- [ ] Create author with missing required fields (should fail)
- [ ] Create author with duplicate name (should fail)
- [ ] Get all authors with pagination
- [ ] Get author by ID
- [ ] Search authors by name
- [ ] Filter authors by birth year
- [ ] Update author
- [ ] Partial update author
- [ ] Delete author
- [ ] Get author statistics

### Quote Operations

- [ ] Create quote with valid data
- [ ] Create quote with invalid author ID (should fail)
- [ ] Create quote with missing required fields (should fail)
- [ ] Get all quotes with pagination
- [ ] Get quote by ID
- [ ] Search quotes by text
- [ ] Search quotes by author name
- [ ] Filter quotes by category
- [ ] Multi-criteria filtering
- [ ] Get all categories
- [ ] Update quote
- [ ] Partial update quote
- [ ] Delete quote

### Validation Tests

- [ ] Test field length constraints
- [ ] Test required field validations
- [ ] Test numeric range validations
- [ ] Test duplicate prevention

### Pagination Tests

- [ ] Test different page sizes
- [ ] Test different page numbers
- [ ] Test sorting by different fields
- [ ] Test ascending/descending order

### Error Handling Tests

- [ ] Test 404 Not Found responses
- [ ] Test 400 Bad Request with validation errors
- [ ] Test 409 Conflict for duplicates
- [ ] Test invalid parameter types

