-- Database verification and statistics queries
-- Run these queries to verify the database setup

-- Check if tables exist
SELECT tablename
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;

-- Count authors
SELECT COUNT(*) as author_count FROM authors;

-- Count quotes
SELECT COUNT(*) as quote_count FROM quotes;

-- Show all authors with their quote counts
SELECT
    a.id,
    a.name,
    a.birth_year,
    a.death_year,
    COUNT(q.id) as quote_count
FROM authors a
LEFT JOIN quotes q ON a.id = q.author_id
GROUP BY a.id, a.name, a.birth_year, a.death_year
ORDER BY a.name;

-- Show all quotes with author names
SELECT
    q.id,
    q.text,
    q.category,
    a.name as author_name
FROM quotes q
JOIN authors a ON q.author_id = a.id
ORDER BY q.id;

-- Show quotes by category
SELECT
    category,
    COUNT(*) as count
FROM quotes
WHERE category IS NOT NULL
GROUP BY category
ORDER BY count DESC, category;

-- Show database size
SELECT
    pg_size_pretty(pg_database_size(current_database())) as database_size;

-- Show table sizes
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
