# ? Complete API Endpoints Reference - Quote REST API

## ? All Available Endpoints (21 Total)

---

## ? AUTHOR ENDPOINTS (10)

### ? Read Operations

#### 1. Get All Authors

```http
GET /api/v1/authors?page=0&size=10&sortBy=name&direction=ASC
```

**Returns:** Paginated list of all authors

#### 2. Get Author by ID

```http
GET /api/v1/authors/{id}
```

**Returns:** Specific author with all their quotes

#### 3. ? **Search Authors by Name** ?

```http
GET /api/v1/authors/search?name={searchTerm}
```

**Features:**
- ? Partial matching (search "Socr" finds "Socrates")
- ? Case-insensitive
- ? Pagination & sorting

**Example:**

```bash
curl "http://localhost:8080/api/v1/authors/search?name=Socrates"
curl "http://localhost:8080/api/v1/authors/search?name=Plat&page=0&size=10"
```

#### 4. Filter Authors by Birth Year

```http
GET /api/v1/authors/filter?birthYear=-469
GET /api/v1/authors/filter?minYear=-500&maxYear=-400
```

**Returns:** Authors by exact year or year range

#### 5. Get Author's Quotes

```http
GET /api/v1/authors/{id}/quotes?page=0&size=10
```

**Returns:** All quotes by specific author

#### 6. Get Author Statistics

```http
GET /api/v1/authors/{id}/stats
```

**Returns:** Author info + quote count

---

### ?? Write Operations

#### 7. Create Author

```http
POST /api/v1/authors
Content-Type: application/json

{
  "name": "Socrates",
  "biography": "Ancient Greek philosopher",
  "birthYear": -469,
  "deathYear": -399
}
```

**Returns:** 201 Created with new author

#### 8. ? **Update Author (Full)** ?

```http
PUT /api/v1/authors/{id}
Content-Type: application/json

{
  "name": "Socrates",
  "biography": "Updated complete biography",
  "birthYear": -469,
  "deathYear": -399
}
```

**Returns:** 200 OK with updated author
**Note:** All fields must be provided

#### 9. ? **Update Author (Partial)** ?

```http
PATCH /api/v1/authors/{id}
Content-Type: application/json

{
  "biography": "Updated biography only"
}
```

**Returns:** 200 OK with updated author
**Note:** Only provided fields are updated

**Examples:**

```bash
# Update just the name
curl -X PATCH http://localhost:8080/api/v1/authors/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Socrates the Great"}'

# Update just the biography
curl -X PATCH http://localhost:8080/api/v1/authors/1 \
  -H "Content-Type: application/json" \
  -d '{"biography": "New biography text"}'

# Update multiple fields
curl -X PATCH http://localhost:8080/api/v1/authors/1 \
  -H "Content-Type: application/json" \
  -d '{"biography": "New bio", "deathYear": -398}'
```

#### 10. Delete Author

```http
DELETE /api/v1/authors/{id}
```

**Returns:** 204 No Content (deletes author and all their quotes)

---

## ? QUOTE ENDPOINTS (11)

### ? Read Operations

#### 1. Get All Quotes

```http
GET /api/v1/quotes?page=0&size=10&sortBy=createdAt&direction=DESC
```

**Returns:** Paginated list of all quotes

#### 2. Get Quote by ID

```http
GET /api/v1/quotes/{id}
```

**Returns:** Specific quote with author details

#### 3. ? **Search Quotes (General)** ?

```http
GET /api/v1/quotes/search?q={searchTerm}
```

**Searches:** Both quote text AND author name

#### 4. Search Quotes by Text

```http
GET /api/v1/quotes/search?text={searchTerm}
```

**Searches:** Quote text only

#### 5. Search Quotes by Author Name

```http
GET /api/v1/quotes/search?author={authorName}
```

**Searches:** Author name only

#### 6. Filter Quotes (Multi-Criteria)

```http
GET /api/v1/quotes/filter?authorId=1&category=Philosophy&searchTerm=life
```

**Filters:** By author ID, category, and/or search term (all optional)

#### 7. Get All Categories

```http
GET /api/v1/quotes/categories
```

**Returns:** List of all distinct categories

---

### ?? Write Operations

#### 8. Create Quote

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

**Returns:** 201 Created with new quote

#### 9. ? **Update Quote (Full)** ?

```http
PUT /api/v1/quotes/{id}
Content-Type: application/json

{
  "text": "Updated quote text",
  "context": "Updated context",
  "category": "Philosophy",
  "authorId": 1
}
```

**Returns:** 200 OK with updated quote

#### 10. ? **Update Quote (Partial)** ?

```http
PATCH /api/v1/quotes/{id}
Content-Type: application/json

{
  "category": "Wisdom"
}
```

**Returns:** 200 OK with updated quote
**Note:** Only provided fields are updated

**Examples:**

```bash
# Update just the category
curl -X PATCH http://localhost:8080/api/v1/quotes/5 \
  -H "Content-Type: application/json" \
  -d '{"category": "Ethics"}'

# Update just the text
curl -X PATCH http://localhost:8080/api/v1/quotes/5 \
  -H "Content-Type: application/json" \
  -d '{"text": "New quote text"}'

# Reassign to different author
curl -X PATCH http://localhost:8080/api/v1/quotes/5 \
  -H "Content-Type: application/json" \
  -d '{"authorId": 2}'
```

#### 11. Delete Quote

```http
DELETE /api/v1/quotes/{id}
```

**Returns:** 204 No Content

---

## ? Key Features Summary

### ? Find/Search Capabilities

|          Feature           |           Endpoint            |        Description        |
|----------------------------|-------------------------------|---------------------------|
| ? **Find Authors by Name** | `GET /authors/search?name=X`  | Partial, case-insensitive |
| ? Search Quotes            | `GET /quotes/search?q=X`      | Text + author search      |
| ? Search Quotes by Text    | `GET /quotes/search?text=X`   | Text only                 |
| ? Search by Author Name    | `GET /quotes/search?author=X` | Author only               |

### ? Edit Capabilities

|           Feature           |       Endpoint        |      Description       |
|-----------------------------|-----------------------|------------------------|
| ? **Edit Author (Full)**    | `PUT /authors/{id}`   | Replace all fields     |
| ? **Edit Author (Partial)** | `PATCH /authors/{id}` | Update selected fields |
| ? **Edit Quote (Full)**     | `PUT /quotes/{id}`    | Replace all fields     |
| ? **Edit Quote (Partial)**  | `PATCH /quotes/{id}`  | Update selected fields |

### ? Additional Features

- ? Pagination (all list endpoints)
- ? Sorting (any field, ASC/DESC)
- ? Filtering (birth year, category, multi-criteria)
- ? Validation (field constraints, business rules)
- ? Error handling (structured JSON responses)
- ? Swagger documentation (interactive testing)

---

## ? Quick Test Commands

### Find Author by Name

```bash
curl "http://localhost:8080/api/v1/authors/search?name=Socrates"
```

### Edit Author Biography

```bash
curl -X PATCH http://localhost:8080/api/v1/authors/1 \
  -H "Content-Type: application/json" \
  -d '{"biography": "Updated biography"}'
```

### Edit Quote Category

```bash
curl -X PATCH http://localhost:8080/api/v1/quotes/5 \
  -H "Content-Type: application/json" \
  -d '{"category": "Wisdom"}'
```

### Search and Edit Workflow

```bash
# 1. Find author
curl "http://localhost:8080/api/v1/authors/search?name=Plato"

# 2. Edit the author (use ID from search result)
curl -X PATCH http://localhost:8080/api/v1/authors/2 \
  -H "Content-Type: application/json" \
  -d '{"biography": "Student of Socrates, teacher of Aristotle"}'

# 3. Verify the update
curl "http://localhost:8080/api/v1/authors/2"
```

---

## ? Implementation Status

### Find Authors by Name

? **Repository:** `findByNameContainingIgnoreCase()`
? **Service:** `searchAuthorsByName()`
? **Controller:** `GET /api/v1/authors/search`
? **Validation:** Parameter validation included
? **Documentation:** Swagger annotations complete
? **Status:** Compiled successfully, ready to use

### Edit Authors

? **Service:** `updateAuthor()`, `patchAuthor()`
? **Controller:** `PUT /api/v1/authors/{id}`, `PATCH /api/v1/authors/{id}`
? **Validation:** Full validation + duplicate check
? **MapStruct:** Automatic entity-DTO mapping
? **Status:** Compiled successfully, ready to use

### Edit Quotes

? **Service:** `updateQuote()`, `patchQuote()`
? **Controller:** `PUT /api/v1/quotes/{id}`, `PATCH /api/v1/quotes/{id}`
? **Validation:** Full validation + author existence check
? **MapStruct:** Automatic entity-DTO mapping
? **Status:** Compiled successfully, ready to use

---

## ? ALL REQUESTED FEATURES ARE READY!

? **Find authors by name** - IMPLEMENTED
? **Edit authors** - IMPLEMENTED (PUT & PATCH)
? **Edit quotes** - IMPLEMENTED (PUT & PATCH)

**Build Status:** ? SUCCESS
**Compilation:** ? 18 files compiled
**Ready to use:** After Step 3 (Database Setup)

---

## ? Documentation Files Created

1. **FIND_AUTHORS_BY_NAME_GUIDE.md** - Complete guide for finding authors
2. **EDIT_ENDPOINTS_GUIDE.md** - Complete guide for editing operations
3. **API_TESTING_GUIDE.md** - All endpoints with examples
4. **STEP2_COMPLETE.md** - Full Step 2 implementation summary

**Everything is documented, compiled, and ready! ?**
