package com.katya.quoterestapi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.katya.quoterestapi.entity.Quote;

/** Repository interface for Quote entity. Provides CRUD operations and custom query methods. */
@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

  /** Find quotes by author ID */
  Page<Quote> findByAuthorId(Long authorId, Pageable pageable);

  /** Find quotes by text containing the given string (case-insensitive) */
  Page<Quote> findByTextContainingIgnoreCase(String text, Pageable pageable);

  /** Find quotes by category */
  Page<Quote> findByCategory(String category, Pageable pageable);

  /** Find quotes by category (case-insensitive) */
  Page<Quote> findByCategoryIgnoreCase(String category, Pageable pageable);

  /** Find quotes by author name containing the given string */
  @Query(
      "SELECT q FROM Quote q WHERE LOWER(q.author.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
  Page<Quote> findByAuthorNameContaining(@Param("authorName") String authorName, Pageable pageable);

  /** Search quotes by text or author name */
  @Query(
      "SELECT q FROM Quote q WHERE "
          + "LOWER(q.text) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(q.author.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Quote> searchQuotes(@Param("searchTerm") String searchTerm, Pageable pageable);

  /** Find quotes with multiple filters */
  @Query(
      value =
          "SELECT q FROM Quote q WHERE "
              + "(:authorId IS NULL OR q.author.id = :authorId) AND "
              + "(:category IS NULL OR LOWER(CAST(q.category AS string)) = LOWER(CAST(:category AS string))) AND "
              + "(:searchTerm IS NULL OR LOWER(CAST(q.text AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')))")
  Page<Quote> findWithFilters(
      @Param("authorId") Long authorId,
      @Param("category") String category,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  /** Get all distinct categories */
  @Query("SELECT DISTINCT q.category FROM Quote q WHERE q.category IS NOT NULL ORDER BY q.category")
  List<String> findAllCategories();
}
