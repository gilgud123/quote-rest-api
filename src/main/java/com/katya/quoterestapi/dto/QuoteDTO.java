package com.katya.quoterestapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO for Quote entity using Java Record.
 * Used for transferring quote data between layers.
 */
@Schema(description = "Quote data transfer object")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuoteDTO(

        @Schema(description = "Unique identifier of the quote", example = "1")
        Long id,

        @NotBlank(message = "Quote text is required")
        @Size(min = 10, max = 2000, message = "Quote text must be between 10 and 2000 characters")
        @Schema(description = "The text of the quote", example = "The unexamined life is not worth living.", required = true)
        String text,

        @Size(max = 500, message = "Context must not exceed 500 characters")
        @Schema(description = "Context or occasion when the quote was said", example = "At his trial")
        String context,

        @Size(max = 100, message = "Category must not exceed 100 characters")
        @Schema(description = "Category of the quote", example = "Philosophy")
        String category,

        @NotNull(message = "Author ID is required")
        @Positive(message = "Author ID must be positive")
        @Schema(description = "ID of the author who said the quote", example = "1", required = true)
        Long authorId,

        @Schema(description = "Name of the author (for display purposes)")
        String authorName,

        @Schema(description = "Timestamp when the quote was created")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp when the quote was last updated")
        LocalDateTime updatedAt
) {

    /**
     * Constructor for creating a new quote without ID (for creation requests)
     */
    public QuoteDTO(String text, String context, String category, Long authorId) {
        this(null, text, context, category, authorId, null, null, null);
    }

    /**
     * Constructor for quote without timestamps (for simplified responses)
     */
    public QuoteDTO(Long id, String text, String context, String category, Long authorId, String authorName) {
        this(id, text, context, category, authorId, authorName, null, null);
    }
}
