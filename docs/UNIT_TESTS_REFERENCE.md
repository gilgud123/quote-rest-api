# Unit Tests - Quick Reference

## Running Tests

### Run All Unit Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=AuthorServiceTest
mvn test -Dtest=QuoteServiceTest
mvn test -Dtest=AuthorMapperTest
mvn test -Dtest=QuoteMapperTest
mvn test -Dtest=GlobalExceptionHandlerTest
```

### Run Single Test Method

```bash
mvn test -Dtest=AuthorServiceTest#shouldGetAuthorById
```

### Run Tests in Package

```bash
mvn test -Dtest="com.katya.quoterestapi.service.*Test"
```

---

## Test Files Created

```
src/test/java/com/katya/quoterestapi/
??? service/
?   ??? AuthorServiceTest.java      ? 17 tests
?   ??? QuoteServiceTest.java       ? 21 tests
??? mapper/
?   ??? AuthorMapperTest.java       ? 8 tests
?   ??? QuoteMapperTest.java        ? 8 tests
??? exception/
    ??? GlobalExceptionHandlerTest.java  ? 5 tests

Total: 5 test classes, 59 test methods
```

---

## Test Coverage Summary

|       Component        | Tests  |            Coverage             |
|------------------------|--------|---------------------------------|
| AuthorService          | 17     | Full CRUD + exceptions          |
| QuoteService           | 21     | Full CRUD + search + exceptions |
| AuthorMapper           | 8      | Entity/DTO conversion + updates |
| QuoteMapper            | 8      | Entity/DTO conversion + updates |
| GlobalExceptionHandler | 5      | All HTTP error codes            |
| **Total**              | **59** | **Comprehensive**               |

---

## What's Tested

### ? AuthorService

- Get all, get by ID, create, update, delete
- Search by name
- Filter by birth year
- Duplicate name validation
- Resource not found exceptions
- Quote count for author

### ? QuoteService

- Get all, get by ID, create, update, delete
- Get by author ID
- Search by text, author name
- Filter by category
- Multi-criteria filtering
- Author validation
- Resource not found exceptions

### ? Mappers

- Entity to DTO conversion
- DTO to Entity conversion
- Partial updates
- Null field handling
- List conversions

### ? Exception Handler

- 404 Not Found
- 409 Conflict
- 400 Bad Request
- Validation errors
- Type conversion errors

---

## ? Step 5: COMPLETE

All unit tests implemented and ready to run!
