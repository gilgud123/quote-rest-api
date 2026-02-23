package com.katya.quoterestapi.mapper;

import com.katya.quoterestapi.dto.QuoteDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for QuoteMapper - Pure unit tests without Spring context
 */
@DisplayName("QuoteMapper Unit Tests")
class QuoteMapperTest {

    private QuoteMapper quoteMapper;

    private Author testAuthor;
    private Quote testQuote;
    private QuoteDTO testQuoteDTO;

    @BeforeEach
    void setUp() {
        // Use MapStruct factory to get mapper instance for unit testing
        quoteMapper = Mappers.getMapper(QuoteMapper.class);

        testAuthor = Author.builder()
                .id(1L)
                .name("Socrates")
                .build();

        testQuote = Quote.builder()
                .id(1L)
                .text("The unexamined life is not worth living.")
                .context("At his trial")
                .category("Philosophy")
                .author(testAuthor)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testQuoteDTO = new QuoteDTO(
                1L,
                "The unexamined life is not worth living.",
                "At his trial",
                "Philosophy",
                1L,
                "Socrates",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should map Quote entity to QuoteDTO")
    void shouldMapEntityToDto() {
        // When
        QuoteDTO result = quoteMapper.toDto(testQuote);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testQuote.getId());
        assertThat(result.text()).isEqualTo(testQuote.getText());
        assertThat(result.context()).isEqualTo(testQuote.getContext());
        assertThat(result.category()).isEqualTo(testQuote.getCategory());
        assertThat(result.authorId()).isEqualTo(testQuote.getAuthor().getId());
        assertThat(result.authorName()).isEqualTo(testQuote.getAuthor().getName());
    }

    @Test
    @DisplayName("Should map QuoteDTO to Quote entity")
    void shouldMapDtoToEntity() {
        // When
        Quote result = quoteMapper.toEntity(testQuoteDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo(testQuoteDTO.text());
        assertThat(result.getContext()).isEqualTo(testQuoteDTO.context());
        assertThat(result.getCategory()).isEqualTo(testQuoteDTO.category());
        assertThat(result.getAuthor()).isNull(); // Author must be set separately in service
    }

    @Test
    @DisplayName("Should update entity from DTO")
    void shouldUpdateEntityFromDto() {
        // Given
        QuoteDTO updateDTO = new QuoteDTO(
                null,
                "Updated quote text",
                "Updated context",
                "Wisdom",
                null,
                null,
                null,
                null
        );

        // When
        quoteMapper.updateEntityFromDto(updateDTO, testQuote);

        // Then
        assertThat(testQuote.getText()).isEqualTo("Updated quote text");
        assertThat(testQuote.getContext()).isEqualTo("Updated context");
        assertThat(testQuote.getCategory()).isEqualTo("Wisdom");
    }

    @Test
    @DisplayName("Should map list of quotes to DTOs")
    void shouldMapListToDtos() {
        // Given
        Quote quote2 = Quote.builder()
                .id(2L)
                .text("I know that I know nothing")
                .author(testAuthor)
                .build();
        List<Quote> quotes = List.of(testQuote, quote2);

        // When
        List<QuoteDTO> result = quoteMapper.toDtoList(quotes);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).text()).contains("unexamined life");
        assertThat(result.get(1).text()).contains("know nothing");
    }

    @Test
    @DisplayName("Should handle null entity")
    void shouldHandleNullEntity() {
        // When
        QuoteDTO result = quoteMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null DTO")
    void shouldHandleNullDto() {
        // When
        Quote result = quoteMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should ignore null fields when updating")
    void shouldIgnoreNullFieldsWhenUpdating() {
        // Given
        String originalContext = testQuote.getContext();
        String originalCategory = testQuote.getCategory();
        QuoteDTO updateDTO = new QuoteDTO(
                null,
                "Updated text only",
                null,  // context is null
                null,  // category is null
                null,
                null,
                null,
                null
        );

        // When
        quoteMapper.updateEntityFromDto(updateDTO, testQuote);

        // Then
        assertThat(testQuote.getText()).isEqualTo("Updated text only");
        assertThat(testQuote.getContext()).isEqualTo(originalContext); // Should remain unchanged
        assertThat(testQuote.getCategory()).isEqualTo(originalCategory); // Should remain unchanged
    }

    @Test
    @DisplayName("Should map author name correctly")
    void shouldMapAuthorNameCorrectly() {
        // When
        QuoteDTO result = quoteMapper.toDto(testQuote);

        // Then
        assertThat(result.authorName()).isEqualTo("Socrates");
        assertThat(result.authorId()).isEqualTo(1L);
    }
}
