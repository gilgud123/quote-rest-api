# MCP Server Setup Guide

This guide explains how to install and configure **Playwright** and **PostgreSQL** MCP (Model Context Protocol) servers for use with GitHub Copilot CLI.

## What are MCP Servers?

MCP servers extend GitHub Copilot CLI's capabilities by providing access to:
- **Playwright MCP**: Browser automation for testing web applications
- **PostgreSQL MCP**: Direct database queries and inspection

## Prerequisites

✅ **Node.js v18+** installed (this project uses v22+)  
✅ **PostgreSQL database** running (Docker or local)  
✅ **GitHub Copilot CLI** installed and authenticated

## Quick Start

### 1. Start PostgreSQL Database

Using Docker (recommended):

```powershell
docker-compose up -d postgres
```

Verify it's running:

```powershell
docker ps
```

You should see `quote-postgres` with status "Up" and "healthy".

### 2. Configure GitHub Copilot CLI

The MCP servers are configured in GitHub Copilot CLI's configuration file.

**Windows Path**: `C:\Users\<YourUsername>\AppData\Roaming\GitHub Copilot\cli\config.json`

**Configuration:**

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": ["-y", "@playwright/mcp-server"]
    },
    "postgres": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-postgres",
        "postgresql://quoteuser:quotepass@localhost:5432/quotedb"
      ]
    }
  }
}
```

### 3. Restart GitHub Copilot CLI

After creating/updating the configuration:

1. Exit any active Copilot CLI sessions
2. Restart your terminal
3. Start a new Copilot CLI session
4. The MCP servers will start automatically

## Usage Examples

### Playwright MCP: Testing the REST API

**Navigate to Swagger UI:**

```
Can you use Playwright to navigate to http://localhost:8080/swagger-ui.html
and take a screenshot?
```

**Test an API Endpoint:**

```
Can you use Playwright to:
1. Go to http://localhost:8080/swagger-ui.html
2. Click on the GET /api/v1/authors endpoint
3. Click "Try it out"
4. Click "Execute"
5. Show me the response
```

**Test with Authentication:**

```
Use Playwright to test the Quote API with authentication:
1. Get a token from Keycloak
2. Add it to the Authorization header
3. Make a POST request to create a new quote
```

### PostgreSQL MCP: Querying the Database

**List All Tables:**

```
Can you query the PostgreSQL database and show me all tables in the quotedb database?
```

**Query Authors:**

```
Query the authors table and show me:
- Total number of authors
- List of all author names
- Authors born before year -400
```

**Query Quotes:**

```
Execute these SQL queries:
1. SELECT COUNT(*) FROM quotes;
2. SELECT * FROM quotes WHERE category = 'Wisdom' LIMIT 5;
3. Show me quotes by Socrates with their categories
```

**Complex Queries:**

```
Can you join the authors and quotes tables to show:
- Each author's name
- Number of quotes they have
- Their oldest quote by creation date
Order by quote count descending
```

## Database Connection Details

**Docker (default):**
- Host: `localhost`
- Port: `5432`
- Database: `quotedb`
- Username: `quoteuser`
- Password: `quotepass`
- Connection String: `postgresql://quoteuser:quotepass@localhost:5432/quotedb`

**If using local PostgreSQL instead of Docker, update the connection string in the config file:**

```json
"postgresql://postgres:postgres@localhost:5432/quotedb"
```

## MCP Server Details

### Playwright MCP Server

**Package**: `@playwright/mcp-server`  
**Repository**: https://github.com/microsoft/playwright-mcp  
**Capabilities**:
- Navigate to URLs
- Click elements
- Fill forms
- Take screenshots
- Execute JavaScript
- Wait for elements
- Handle authentication

**Browser**: Chromium (default)

### PostgreSQL MCP Server

**Package**: `@modelcontextprotocol/server-postgres`  
**Repository**: https://github.com/modelcontextprotocol/servers  
**Capabilities**:
- Execute SELECT queries
- View table schemas
- List databases and tables
- Join queries
- Aggregate functions
- Read-only operations (safe)

**Security**: Only SELECT queries allowed (no INSERT/UPDATE/DELETE)

## Verification

### Check MCP Servers are Running

The MCP servers should be automatically detected by GitHub Copilot CLI when properly configured.

### Test Playwright

```
Ask Copilot: "Use Playwright to navigate to https://google.com and tell me the page title"
```

### Test PostgreSQL

```
Ask Copilot: "Query the PostgreSQL database and count the rows in the authors table"
```

## Troubleshooting

### MCP Servers Not Showing Up

**Problem**: GitHub Copilot CLI doesn't detect MCP servers

**Solutions**:
1. Verify config file location: `%APPDATA%\GitHub Copilot\cli\config.json`
2. Check JSON syntax is valid (use a JSON validator)
3. Restart your terminal and start a new Copilot CLI session
4. Run `gh copilot --version` to verify CLI installation
5. Check that Node.js is available: `node --version`

### Playwright Fails to Start

**Problem**: Playwright MCP server fails with "browser not found"

**Solution**: Install Playwright browsers manually:

```powershell
npx playwright install chromium
```

### PostgreSQL Connection Failed

**Problem**: PostgreSQL MCP cannot connect to database

**Solutions**:
1. Verify PostgreSQL is running: `docker ps`
2. Check database is accessible: `docker exec -it quote-postgres psql -U quoteuser -d quotedb`
3. Verify connection string in config file
4. Check firewall isn't blocking port 5432

### Permission Issues

**Problem**: "Cannot execute npx" or permission denied

**Solution**: Ensure Node.js is in PATH and npx has execute permissions

### Database Not Started

**Problem**: PostgreSQL queries fail with "connection refused"

**Solution**:

```powershell
# Start PostgreSQL
docker-compose up -d postgres

# Wait for it to be healthy
docker ps

# Test connection
docker exec -it quote-postgres psql -U quoteuser -d quotedb -c "\dt"
```

## Advanced Configuration

### Using Different PostgreSQL Credentials

If you're using local PostgreSQL with different credentials:

```json
{
  "mcpServers": {
    "postgres": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-postgres",
        "postgresql://YOUR_USER:YOUR_PASSWORD@localhost:5432/YOUR_DATABASE"
      ]
    }
  }
}
```

### Adding Environment Variables

For sensitive credentials, use environment variables:

```json
{
  "mcpServers": {
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "POSTGRES_CONNECTION_STRING": "postgresql://quoteuser:quotepass@localhost:5432/quotedb"
      }
    }
  }
}
```

### Using Different Browsers with Playwright

Playwright supports Chromium, Firefox, and WebKit. To specify a different browser, the server would need custom configuration (refer to Playwright MCP documentation).

## Integration with Development Workflow

### 1. Testing API Endpoints

Use Playwright to automate testing of your REST API through the Swagger UI:
- Test all endpoints without writing test code
- Verify responses and status codes
- Test authentication flows
- Screenshot API responses for documentation

### 2. Database Inspection

Use PostgreSQL MCP to:
- Verify data after running migrations
- Check seed data is loaded correctly
- Inspect relationships between tables
- Debug data issues quickly

### 3. End-to-End Testing

Combine both MCP servers:
1. Use PostgreSQL to setup test data
2. Use Playwright to test UI interactions
3. Use PostgreSQL to verify data changes
4. Document results with screenshots

## Useful SQL Queries

### Schema Inspection

```sql
-- List all tables
\dt

-- Describe authors table
\d authors

-- Describe quotes table  
\d quotes
```

### Data Queries

```sql
-- Count authors and quotes
SELECT 
  (SELECT COUNT(*) FROM authors) as author_count,
  (SELECT COUNT(*) FROM quotes) as quote_count;

-- Authors with most quotes
SELECT 
  a.name, 
  COUNT(q.id) as quote_count
FROM authors a
LEFT JOIN quotes q ON a.id = q.author_id
GROUP BY a.id, a.name
ORDER BY quote_count DESC;

-- Quotes by category
SELECT 
  category, 
  COUNT(*) as count
FROM quotes
GROUP BY category
ORDER BY count DESC;

-- Recent quotes
SELECT 
  q.text,
  a.name as author,
  q.category,
  q.created_at
FROM quotes q
JOIN authors a ON q.author_id = a.id
ORDER BY q.created_at DESC
LIMIT 10;
```

## Security Considerations

### PostgreSQL MCP Security

✅ **Read-only access**: MCP server only allows SELECT queries  
✅ **No data modification**: Cannot INSERT, UPDATE, or DELETE  
✅ **Connection string in config**: Consider using environment variables for production

### Playwright MCP Security

⚠️ **Full browser access**: Can navigate to any URL  
⚠️ **Local network access**: Can access localhost services  
✅ **No persistent state**: Each session is isolated

## Resources

- **MCP Specification**: https://modelcontextprotocol.io/
- **Playwright Documentation**: https://playwright.dev/
- **PostgreSQL Documentation**: https://www.postgresql.org/docs/
- **Quote REST API README**: [README.md](../README.md)

## Next Steps

1. ✅ MCP servers configured
2. ✅ PostgreSQL database running
3. ⏭️ Test API endpoints via Playwright
4. ⏭️ Query database via PostgreSQL MCP
5. ⏭️ Integrate into development workflow
6. ⏭️ Share setup with team members

---

**Need Help?**

- Check the MCP server logs for any errors
- Verify PostgreSQL is running: `docker ps`
- Test Node.js/npx: `node --version` and `npx --version`
- Refer to troubleshooting section above
