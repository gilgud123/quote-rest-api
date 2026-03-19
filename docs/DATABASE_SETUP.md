# Database Setup Guide - PostgreSQL

## Overview

This guide covers setting up PostgreSQL database for the Quote REST API with schema and sample data.

---

## Option 1: Using Docker Compose (Recommended)

This will be covered in Step 4 - easiest way to get started.

---

## Option 2: Manual PostgreSQL Setup

### Prerequisites

- PostgreSQL 12+ installed on your system
- psql command-line tool available

### Step 1: Run the Initialization Script

#### On Windows (PowerShell):

```powershell
cd "C:\Users\Katya de Vries\IdeaProjects\quote-rest-api"
.\scripts\init-db.ps1
```

#### On Linux/Mac (Bash):

```bash
cd /path/to/quote-rest-api
chmod +x scripts/init-db.sh
./scripts/init-db.sh
```

This script will:
- Create database: `quotedb`
- Create user: `quoteuser` with password: `quotepass`
- Grant necessary privileges

### Step 2: Start the Application

The application will automatically:
- Execute `schema.sql` (create tables and indexes)
- Execute `data.sql` (insert sample data)

```bash
mvn spring-boot:run
```

---

## Option 3: Manual Database Creation

### Step 1: Connect to PostgreSQL

```bash
psql -U postgres
```

### Step 2: Create Database and User

```sql
-- Create database
CREATE DATABASE quotedb;

-- Create user
CREATE USER quoteuser WITH PASSWORD 'quotepass';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE quotedb TO quoteuser;

-- Connect to the database
\c quotedb

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO quoteuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO quoteuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO quoteuser;
```

### Step 3: Verify Connection

```bash
psql -U quoteuser -d quotedb -W
```

Enter password: `quotepass`

---

## Database Configuration

### Default Configuration (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/quotedb
    username: postgres
    password: postgres
```

### Docker Configuration (application-docker.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/quotedb
    username: quoteuser
    password: quotepass
```

---

## Database Schema

### Tables Created

#### authors

|   Column   |     Type      |       Constraints       |
|------------|---------------|-------------------------|
| id         | BIGSERIAL     | PRIMARY KEY             |
| name       | VARCHAR(100)  | NOT NULL, UNIQUE        |
| biography  | VARCHAR(1000) |                         |
| birth_year | INTEGER       | CHECK (-500 to 2100)    |
| death_year | INTEGER       | CHECK (-500 to 2100)    |
| created_at | TIMESTAMP     | NOT NULL, DEFAULT NOW() |
| updated_at | TIMESTAMP     | NOT NULL, DEFAULT NOW() |

#### quotes

|   Column   |     Type      |       Constraints       |
|------------|---------------|-------------------------|
| id         | BIGSERIAL     | PRIMARY KEY             |
| text       | VARCHAR(2000) | NOT NULL                |
| context    | VARCHAR(500)  |                         |
| category   | VARCHAR(100)  |                         |
| author_id  | BIGINT        | NOT NULL, FOREIGN KEY   |
| created_at | TIMESTAMP     | NOT NULL, DEFAULT NOW() |
| updated_at | TIMESTAMP     | NOT NULL, DEFAULT NOW() |

### Indexes

- `idx_authors_name` - On authors.name
- `idx_authors_birth_year` - On authors.birth_year
- `idx_quotes_author_id` - On quotes.author_id
- `idx_quotes_category` - On quotes.category
- `idx_quotes_text` - Full-text search index (GIN)

### Foreign Key

- `quotes.author_id` ? `authors.id` (CASCADE on delete)

---

## Sample Data

### Authors (3)

1. **Socrates** (-469 to -399)
2. **Plato** (-428 to -348)
3. **Aristotle** (-384 to -322)

### Quotes (10 total)

- **Socrates:** 4 quotes
- **Plato:** 4 quotes
- **Aristotle:** 2 quotes

### Categories

- Philosophy
- Wisdom
- Self-Improvement
- Humor
- Politics
- Education
- Excellence

---

## Verification

### Verify Database Setup

Run the verification SQL script:

```bash
psql -U quoteuser -d quotedb -f scripts/verify-db.sql
```

Or manually:

```sql
-- Check tables
\dt

-- Count records
SELECT COUNT(*) FROM authors;  -- Should return 3
SELECT COUNT(*) FROM quotes;   -- Should return 10

-- View all data
SELECT * FROM authors;
SELECT * FROM quotes;
```

### Test Application Connection

Start the application:

```bash
mvn spring-boot:run
```

Check logs for successful connection:

```
Hikari - Start completed.
Initialized JPA EntityManagerFactory
```

---

## Troubleshooting

### Issue: Connection refused

**Solution:** Ensure PostgreSQL is running

```bash
# Windows
Get-Service postgresql*

# Linux
sudo systemctl status postgresql

# Mac
brew services list
```

### Issue: Authentication failed

**Solution:** Check username/password in application.yml

### Issue: Database does not exist

**Solution:** Run the init-db script first

### Issue: Permission denied

**Solution:** Grant proper privileges:

```sql
GRANT ALL ON SCHEMA public TO quoteuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO quoteuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO quoteuser;
```

### Issue: Schema not initialized

**Solution:** Check application.yml:

```yaml
spring:
  sql:
    init:
      mode: always
```

---

## Profiles

### Development (default)

```bash
mvn spring-boot:run
```

Uses `application.yml` (localhost PostgreSQL)

### Docker

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

Uses `application-docker.yml` (postgres hostname)

### Production

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Uses `application-prod.yml` (environment variables)

---

## Environment Variables (Production)

```bash
export DATABASE_URL=jdbc:postgresql://your-host:5432/quotedb
export DATABASE_USERNAME=quoteuser
export DATABASE_PASSWORD=secure-password
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Next Steps

After database setup:
1. ? Database created and configured
2. ? Schema and data loaded
3. ?? Start application: `mvn spring-boot:run`
4. ?? Test endpoints: http://localhost:8080/swagger-ui.html
5. ?? Proceed to Step 4: Docker configuration
