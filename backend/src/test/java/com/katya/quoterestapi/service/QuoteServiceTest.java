package com.katya.quoterestapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.katya.quoterestapi.dto.QuoteDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
import com.katya.quoterestapi.exception.ResourceNotFoundException;
import com.katya.quoterestapi.mapper.QuoteMapper;
import com.katya.quoterestapi.repository.AuthorRepository;
import com.katya.quoterestapi.repository.QuoteRepository;

/** Unit tests for QuoteService */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuoteService Unit Tests")
class QuoteServiceTest {

  @Mock private QuoteRepository quoteRepository;

  @Mock private AuthorRepository authorRepository;

  @Mock private QuoteMapper quoteMapper;

  @InjectMocks private QuoteService quoteService;

  private Author testAuthor;
  private Quote testQuote;
  private QuoteDTO testQuoteDTO;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testAuthor =
        Author.builder().id(1L).name("Socrates").biography("Ancient Greek philosopher").build();

    testQuote =
        Quote.builder()
            .id(1L)
            .text("The unexamined life is not worth living.")
            .context("At his trial")
            .category("Philosophy")
            .author(testAuthor)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    testQuoteDTO =
        new QuoteDTO(
            1L,
            "The unexamined life is not worth living.",
            "At his trial",
            "Philosophy",
            1L,
            "Socrates",
            LocalDateTime.now(),
            LocalDateTime.now());

    pageable = PageRequest.of(0, 10);
  }

  @Test
  @DisplayName("Should get all quotes with pagination")
  void shouldGetAllQuotes() {
    // Given
    Page<Quote> quotePage = new PageImpl<>(List.of(testQuote));
    when(quoteRepository.findAll(pageable)).thenReturn(quotePage);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    Page<QuoteDTO> result = quoteService.getAllQuotes(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).text()).contains("unexamined life");
    verify(quoteRepository).findAll(pageable);
    verify(quoteMapper).toDto(testQuote);
  }

  @Test
  @DisplayName("Should get quote by ID")
  void shouldGetQuoteById() {
    // Given
    when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    QuoteDTO result = quoteService.getQuoteById(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.text()).isEqualTo("The unexamined life is not worth living.");
    verify(quoteRepository).findById(1L);
    verify(quoteMapper).toDto(testQuote);
  }

  @Test
  @DisplayName("Should throw exception when quote not found by ID")
  void shouldThrowExceptionWhenQuoteNotFound() {
    // Given
    when(quoteRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> quoteService.getQuoteById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Quote not found with id: 999");
    verify(quoteRepository).findById(999L);
    verify(quoteMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("Should get quotes by author ID")
  void shouldGetQuotesByAuthorId() {
    // Given
    when(authorRepository.existsById(1L)).thenReturn(true);
    Page<Quote> quotePage = new PageImpl<>(List.of(testQuote));
    when(quoteRepository.findByAuthorId(1L, pageable)).thenReturn(quotePage);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    Page<QuoteDTO> result = quoteService.getQuotesByAuthorId(1L, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).authorId()).isEqualTo(1L);
    verify(authorRepository).existsById(1L);
    verify(quoteRepository).findByAuthorId(1L, pageable);
  }

  @Test
  @DisplayName("Should throw exception when getting quotes for non-existent author")
  void shouldThrowExceptionWhenAuthorNotFoundForQuotes() {
    // Given
    when(authorRepository.existsById(999L)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> quoteService.getQuotesByAuthorId(999L, pageable))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author not found with id: 999");
    verify(authorRepository).existsById(999L);
    verify(quoteRepository, never()).findByAuthorId(any(), any());
  }

  @Test
  @DisplayName("Should search quotes by text")
  void shouldSearchQuotesByText() {
    // Given
    String searchText = "life";
    Page<Quote> quotePage = new PageImpl<>(List.of(testQuote));
    when(quoteRepository.findByTextContainingIgnoreCase(searchText, pageable))
        .thenReturn(quotePage);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    Page<QuoteDTO> result = quoteService.searchQuotesByText(searchText, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(quoteRepository).findByTextContainingIgnoreCase(searchText, pageable);
  }

  @Test
  @DisplayName("Should filter quotes by category")
  void shouldFilterQuotesByCategory() {
    // Given
    String category = "Philosophy";
    Page<Quote> quotePage = new PageImpl<>(List.of(testQuote));
    when(quoteRepository.findByCategoryIgnoreCase(category, pageable)).thenReturn(quotePage);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    Page<QuoteDTO> result = quoteService.filterQuotesByCategory(category, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).category()).isEqualTo("Philosophy");
    verify(quoteRepository).findByCategoryIgnoreCase(category, pageable);
  }

  @Test
  @DisplayName("Should get all categories")
  void shouldGetAllCategories() {
    // Given
    List<String> categories = Arrays.asList("Philosophy", "Wisdom", "Politics");
    when(quoteRepository.findAllCategories()).thenReturn(categories);

    // When
    List<String> result = quoteService.getAllCategories();

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);
    assertThat(result).contains("Philosophy", "Wisdom", "Politics");
    verify(quoteRepository).findAllCategories();
  }

  @Test
  @DisplayName("Should create new quote")
  void shouldCreateQuote() {
    // Given
    QuoteDTO createDTO = new QuoteDTO("New quote text", "Context", "Philosophy", 1L);
    Quote newQuote = Quote.builder().text("New quote text").build();
    Quote savedQuote = Quote.builder().id(2L).text("New quote text").author(testAuthor).build();
    QuoteDTO savedDTO =
        new QuoteDTO(2L, "New quote text", "Context", "Philosophy", 1L, "Socrates", null, null);

    when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
    when(quoteMapper.toEntity(createDTO)).thenReturn(newQuote);
    when(quoteRepository.save(any(Quote.class))).thenReturn(savedQuote);
    when(quoteMapper.toDto(savedQuote)).thenReturn(savedDTO);

    // When
    QuoteDTO result = quoteService.createQuote(createDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(2L);
    assertThat(result.text()).isEqualTo("New quote text");
    verify(authorRepository).findById(1L);
    verify(quoteRepository).save(any(Quote.class));
  }

  @Test
  @DisplayName("Should throw exception when creating quote with non-existent author")
  void shouldThrowExceptionWhenCreatingQuoteWithInvalidAuthor() {
    // Given
    QuoteDTO createDTO = new QuoteDTO("Quote", "Context", "Category", 999L);
    when(authorRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> quoteService.createQuote(createDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author not found with id: 999");
    verify(authorRepository).findById(999L);
    verify(quoteRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update existing quote")
  void shouldUpdateQuote() {
    // Given
    QuoteDTO updateDTO =
        new QuoteDTO(null, "Updated text", "Updated context", "Philosophy", 1L, null, null, null);
    when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
    when(quoteRepository.save(testQuote)).thenReturn(testQuote);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    QuoteDTO result = quoteService.updateQuote(1L, updateDTO);

    // Then
    assertThat(result).isNotNull();
    verify(quoteRepository).findById(1L);
    verify(quoteMapper).updateEntityFromDto(updateDTO, testQuote);
    verify(quoteRepository).save(testQuote);
  }

  @Test
  @DisplayName("Should update quote with different author")
  void shouldUpdateQuoteWithDifferentAuthor() {
    // Given
    Author newAuthor = Author.builder().id(2L).name("Plato").build();
    QuoteDTO updateDTO =
        new QuoteDTO(null, "Updated text", null, "Philosophy", 2L, null, null, null);

    when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
    when(authorRepository.findById(2L)).thenReturn(Optional.of(newAuthor));
    when(quoteRepository.save(testQuote)).thenReturn(testQuote);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    QuoteDTO result = quoteService.updateQuote(1L, updateDTO);

    // Then
    assertThat(result).isNotNull();
    verify(quoteRepository).findById(1L);
    verify(authorRepository).findById(2L);
    verify(quoteRepository).save(testQuote);
  }

  @Test
  @DisplayName("Should delete quote")
  void shouldDeleteQuote() {
    // Given
    when(quoteRepository.existsById(1L)).thenReturn(true);
    doNothing().when(quoteRepository).deleteById(1L);

    // When
    quoteService.deleteQuote(1L);

    // Then
    verify(quoteRepository).existsById(1L);
    verify(quoteRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent quote")
  void shouldThrowExceptionWhenDeletingNonExistentQuote() {
    // Given
    when(quoteRepository.existsById(999L)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> quoteService.deleteQuote(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Quote not found with id: 999");
    verify(quoteRepository).existsById(999L);
    verify(quoteRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should filter quotes with multiple criteria")
  void shouldFilterQuotesWithMultipleCriteria() {
    // Given
    Long authorId = 1L;
    String category = "Philosophy";
    String searchTerm = "life";

    when(authorRepository.existsById(1L)).thenReturn(true);
    Page<Quote> quotePage = new PageImpl<>(List.of(testQuote));
    when(quoteRepository.findWithFilters(authorId, category, searchTerm, pageable))
        .thenReturn(quotePage);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    Page<QuoteDTO> result = quoteService.filterQuotes(authorId, category, searchTerm, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(authorRepository).existsById(1L);
    verify(quoteRepository).findWithFilters(authorId, category, searchTerm, pageable);
  }

  @Test
  @DisplayName("Should filter quotes without author validation when authorId is null")
  void shouldFilterQuotesWithoutAuthorValidation() {
    // Given
    String category = "Philosophy";
    Page<Quote> quotePage = new PageImpl<>(List.of(testQuote));
    when(quoteRepository.findWithFilters(null, category, null, pageable)).thenReturn(quotePage);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    // When
    Page<QuoteDTO> result = quoteService.filterQuotes(null, category, null, pageable);

    // Then
    assertThat(result).isNotNull();
    verify(authorRepository, never()).existsById(any());
    verify(quoteRepository).findWithFilters(null, category, null, pageable);
  }

  @Test
  @DisplayName("Should search quotes by author name")
  void shouldSearchQuotesByAuthorName() {
    String authorName = "Socrates";
    Page<Quote> quotePage = new PageImpl<>(List.of(testQuote));
    when(quoteRepository.findByAuthorNameContaining(authorName, pageable)).thenReturn(quotePage);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    Page<QuoteDTO> result = quoteService.searchQuotesByAuthorName(authorName, pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(quoteRepository).findByAuthorNameContaining(authorName, pageable);
  }

  @Test
  @DisplayName("Should perform general search across quotes")
  void shouldSearchQuotes() {
    String searchTerm = "life";
    Page<Quote> quotePage = new PageImpl<>(List.of(testQuote));
    when(quoteRepository.searchQuotes(searchTerm, pageable)).thenReturn(quotePage);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    Page<QuoteDTO> result = quoteService.searchQuotes(searchTerm, pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(quoteRepository).searchQuotes(searchTerm, pageable);
  }

  @Test
  @DisplayName("Should throw exception when filtering with missing author")
  void shouldThrowExceptionWhenFilteringWithMissingAuthor() {
    when(authorRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> quoteService.filterQuotes(999L, "Philosophy", null, pageable))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author not found with id: 999");
    verify(authorRepository).existsById(999L);
    verify(quoteRepository, never()).findWithFilters(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Should throw exception when updating quote with missing new author")
  void shouldThrowExceptionWhenUpdatingWithMissingAuthor() {
    QuoteDTO updateDTO =
        new QuoteDTO(null, "Updated text", null, "Philosophy", 2L, null, null, null);
    when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
    when(authorRepository.findById(2L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> quoteService.updateQuote(1L, updateDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author not found with id: 2");
    verify(quoteRepository).findById(1L);
    verify(authorRepository).findById(2L);
    verify(quoteRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should patch quote using update logic")
  void shouldPatchQuote() {
    QuoteDTO patchDTO = new QuoteDTO(null, "Patched text", null, "Wisdom", 1L, null, null, null);
    when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
    when(quoteRepository.save(testQuote)).thenReturn(testQuote);
    when(quoteMapper.toDto(testQuote)).thenReturn(testQuoteDTO);

    QuoteDTO result = quoteService.patchQuote(1L, patchDTO);

    assertThat(result).isNotNull();
    verify(quoteRepository).findById(1L);
    verify(quoteMapper).updateEntityFromDto(patchDTO, testQuote);
    verify(quoteRepository).save(testQuote);
  }
}
