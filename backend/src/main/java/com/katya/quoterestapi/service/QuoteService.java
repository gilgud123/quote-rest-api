package com.katya.quoterestapi.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.katya.quoterestapi.dto.QuoteDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
import com.katya.quoterestapi.exception.ResourceNotFoundException;
import com.katya.quoterestapi.mapper.QuoteMapper;
import com.katya.quoterestapi.repository.AuthorRepository;
import com.katya.quoterestapi.repository.QuoteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service class for Quote-related business logic. */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuoteService {

  private final QuoteRepository quoteRepository;
  private final AuthorRepository authorRepository;
  private final QuoteMapper quoteMapper;

  /** Get all quotes with pagination */
  public Page<QuoteDTO> getAllQuotes(Pageable pageable) {
    log.debug("Fetching all quotes with pagination: {}", pageable);
    return quoteRepository.findAll(pageable).map(quoteMapper::toDto);
  }

  /** Get quote by ID */
  public QuoteDTO getQuoteById(Long id) {
    log.debug("Fetching quote with id: {}", id);
    Quote quote =
        quoteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quote", id));
    return quoteMapper.toDto(quote);
  }

  /** Get all quotes by author ID */
  public Page<QuoteDTO> getQuotesByAuthorId(Long authorId, Pageable pageable) {
    log.debug("Fetching quotes for author with id: {}", authorId);

    // Verify author exists
    if (!authorRepository.existsById(authorId)) {
      throw new ResourceNotFoundException("Author", authorId);
    }

    return quoteRepository.findByAuthorId(authorId, pageable).map(quoteMapper::toDto);
  }

  /** Search quotes by text */
  public Page<QuoteDTO> searchQuotesByText(String text, Pageable pageable) {
    log.debug("Searching quotes by text: {}", text);
    return quoteRepository.findByTextContainingIgnoreCase(text, pageable).map(quoteMapper::toDto);
  }

  /** Filter quotes by category */
  public Page<QuoteDTO> filterQuotesByCategory(String category, Pageable pageable) {
    log.debug("Filtering quotes by category: {}", category);
    return quoteRepository.findByCategoryIgnoreCase(category, pageable).map(quoteMapper::toDto);
  }

  /** Search quotes by author name */
  public Page<QuoteDTO> searchQuotesByAuthorName(String authorName, Pageable pageable) {
    log.debug("Searching quotes by author name: {}", authorName);
    return quoteRepository.findByAuthorNameContaining(authorName, pageable).map(quoteMapper::toDto);
  }

  /** General search across quotes (text and author name) */
  public Page<QuoteDTO> searchQuotes(String searchTerm, Pageable pageable) {
    log.debug("Searching quotes with term: {}", searchTerm);
    return quoteRepository.searchQuotes(searchTerm, pageable).map(quoteMapper::toDto);
  }

  /** Filter quotes with multiple criteria */
  public Page<QuoteDTO> filterQuotes(
      Long authorId, String category, String searchTerm, Pageable pageable) {
    log.debug(
        "Filtering quotes with authorId: {}, category: {}, searchTerm: {}",
        authorId,
        category,
        searchTerm);

    // Verify author exists if authorId is provided
    if (authorId != null && !authorRepository.existsById(authorId)) {
      throw new ResourceNotFoundException("Author", authorId);
    }

    return quoteRepository
        .findWithFilters(authorId, category, searchTerm, pageable)
        .map(quoteMapper::toDto);
  }

  /** Get all distinct categories */
  public List<String> getAllCategories() {
    log.debug("Fetching all distinct categories");
    return quoteRepository.findAllCategories();
  }

  /** Create a new quote */
  @Transactional
  public QuoteDTO createQuote(QuoteDTO quoteDTO) {
    log.debug("Creating new quote for author with id: {}", quoteDTO.authorId());

    // Verify author exists
    Author author =
        authorRepository
            .findById(quoteDTO.authorId())
            .orElseThrow(() -> new ResourceNotFoundException("Author", quoteDTO.authorId()));

    Quote quote = quoteMapper.toEntity(quoteDTO);
    quote.setAuthor(author);

    Quote savedQuote = quoteRepository.save(quote);
    log.info("Created quote with id: {}", savedQuote.getId());

    return quoteMapper.toDto(savedQuote);
  }

  /** Update an existing quote */
  @Transactional
  public QuoteDTO updateQuote(Long id, QuoteDTO quoteDTO) {
    log.debug("Updating quote with id: {}", id);

    Quote existingQuote =
        quoteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quote", id));

    // If authorId is being changed, verify the new author exists
    if (quoteDTO.authorId() != null
        && !quoteDTO.authorId().equals(existingQuote.getAuthor().getId())) {
      Author newAuthor =
          authorRepository
              .findById(quoteDTO.authorId())
              .orElseThrow(() -> new ResourceNotFoundException("Author", quoteDTO.authorId()));
      existingQuote.setAuthor(newAuthor);
    }

    quoteMapper.updateEntityFromDto(quoteDTO, existingQuote);
    Quote updatedQuote = quoteRepository.save(existingQuote);
    log.info("Updated quote with id: {}", id);

    return quoteMapper.toDto(updatedQuote);
  }

  /** Partially update an existing quote */
  @Transactional
  public QuoteDTO patchQuote(Long id, QuoteDTO quoteDTO) {
    log.debug("Partially updating quote with id: {}", id);
    return updateQuote(id, quoteDTO); // Using the same logic for patch
  }

  /** Delete a quote by ID */
  @Transactional
  public void deleteQuote(Long id) {
    log.debug("Deleting quote with id: {}", id);

    if (!quoteRepository.existsById(id)) {
      throw new ResourceNotFoundException("Quote", id);
    }

    quoteRepository.deleteById(id);
    log.info("Deleted quote with id: {}", id);
  }
}
