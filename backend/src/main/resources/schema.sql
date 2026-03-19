-- Database Schema for Quote REST API
-- PostgreSQL Database Schema

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS quotes CASCADE;
DROP TABLE IF EXISTS authors CASCADE;

-- Create Authors table
CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    biography VARCHAR(1000),
    birth_year INTEGER CHECK (birth_year >= -500 AND birth_year <= 2100),
    death_year INTEGER CHECK (death_year >= -500 AND death_year <= 2100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Quotes table
CREATE TABLE quotes (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(2000) NOT NULL,
    context VARCHAR(500),
    category VARCHAR(100),
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_authors_name ON authors(name);
CREATE INDEX idx_authors_birth_year ON authors(birth_year);
CREATE INDEX idx_quotes_author_id ON quotes(author_id);
CREATE INDEX idx_quotes_category ON quotes(category);
CREATE INDEX idx_quotes_text ON quotes USING gin(to_tsvector('english', text));

-- Add comments to tables and columns for documentation
COMMENT ON TABLE authors IS 'Stores information about quote authors';
COMMENT ON COLUMN authors.id IS 'Unique identifier for the author';
COMMENT ON COLUMN authors.name IS 'Full name of the author';
COMMENT ON COLUMN authors.biography IS 'Brief biography of the author';
COMMENT ON COLUMN authors.birth_year IS 'Year the author was born (negative for BCE)';
COMMENT ON COLUMN authors.death_year IS 'Year the author died (negative for BCE)';
COMMENT ON COLUMN authors.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN authors.updated_at IS 'Timestamp when the record was last updated';

COMMENT ON TABLE quotes IS 'Stores quotes said or written by authors';
COMMENT ON COLUMN quotes.id IS 'Unique identifier for the quote';
COMMENT ON COLUMN quotes.text IS 'The actual text of the quote';
COMMENT ON COLUMN quotes.context IS 'Context or occasion when the quote was said';
COMMENT ON COLUMN quotes.category IS 'Category or theme of the quote';
COMMENT ON COLUMN quotes.author_id IS 'Foreign key reference to the author';
COMMENT ON COLUMN quotes.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN quotes.updated_at IS 'Timestamp when the record was last updated';
