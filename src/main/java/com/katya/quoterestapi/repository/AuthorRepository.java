package com.katya.quoterestapi.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.katya.quoterestapi.entity.Author;

/** Repository interface for Author entity. Provides CRUD operations and custom query methods. */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

  /** Find authors by name containing the given string (case-insensitive) */
  Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);

  /** Find authors by birth year */
  Page<Author> findByBirthYear(Integer birthYear, Pageable pageable);

  /** Find authors by birth year range */
  @Query(
      "SELECT a FROM Author a WHERE "
          + "(:minYear IS NULL OR a.birthYear >= :minYear) AND "
          + "(:maxYear IS NULL OR a.birthYear <= :maxYear)")
  Page<Author> findByBirthYearBetween(
      @Param("minYear") Integer minYear, @Param("maxYear") Integer maxYear, Pageable pageable);

  /** Find author by name (exact match) */
  Optional<Author> findByName(String name);

  /** Check if author exists by name */
  boolean existsByName(String name);

  /** Count quotes for an author */
  @Query("SELECT COUNT(q) FROM Quote q WHERE q.author.id = :authorId")
  long countQuotesByAuthorId(@Param("authorId") Long authorId);
}
