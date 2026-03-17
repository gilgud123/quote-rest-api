# Database Files - Quick Reference

## Files Created in src/main/resources/

### schema.sql

Creates the database structure:
- **authors** table (7 columns)
- **quotes** table (7 columns)
- 5 indexes for performance
- Foreign key constraint (CASCADE delete)
- Table and column comments

### data.sql

Inserts sample data:
- **3 authors:** Socrates, Plato, Aristotle
- **10 quotes:** 4 from Socrates, 4 from Plato, 2 from Aristotle
- **7 categories:** Philosophy, Wisdom, Self-Improvement, Humor, Politics, Education, Excellence

## Profiles Created

### application-docker.yml

- Database host: `postgres` (Docker service name)
- Username: `quoteuser`
- Password: `quotepass`
- Use with: `-Dspring.profiles.active=docker`

### application-dev.yml

- Database host: `localhost`
- Username: `postgres`
- Password: `postgres`
- Verbose logging
- Use with: `-Dspring.profiles.active=dev`

### application-prod.yml

- Environment variable based config
- Connection pooling optimized
- Minimal logging
- Schema validation only (no auto-init)
- Use with: `-Dspring.profiles.active=prod`

## Scripts Created in scripts/

### init-db.ps1 (Windows)

PowerShell script to:
- Create database `quotedb`
- Create user `quoteuser`
- Grant privileges

### init-db.sh (Linux/Mac)

Bash script to:
- Create database `quotedb`
- Create user `quoteuser`
- Grant privileges

### verify-db.sql

Verification queries:
- List tables
- Count records
- Show authors with quote counts
- Show quotes with authors
- Display statistics

## Database Schema Details

### authors Table

```sql
id          BIGSERIAL PRIMARY KEY
name        VARCHAR(100) NOT NULL UNIQUE
biography   VARCHAR(1000)
birth_year  INTEGER CHECK (-500 to 2100)
death_year  INTEGER CHECK (-500 to 2100)
created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

### quotes Table

```sql
id          BIGSERIAL PRIMARY KEY
text        VARCHAR(2000) NOT NULL
context     VARCHAR(500)
category    VARCHAR(100)
author_id   BIGINT NOT NULL REFERENCES authors(id) ON DELETE CASCADE
created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

## Quick Start

### With existing PostgreSQL:

```bash
.\scripts\init-db.ps1
mvn spring-boot:run
```

### With Docker (Step 4):

```bash
docker-compose up
```

The schema and data will load automatically on application startup.

## ? Step 3 Complete

All database files are created and ready to use!
