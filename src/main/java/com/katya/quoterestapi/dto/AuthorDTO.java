package com.katya.quoterestapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Author entity using Java Record.
 * Used for transferring author data between layers.
 */
@Schema(description = "Author data transfer object")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorDTO(

        @Schema(description = "Unique identifier of the author", example = "1")
        Long id,

        @NotBlank(message = "Author name is required")
        @Size(min = 2, max = 100, message = "Author name must be between 2 and 100 characters")
        @Schema(description = "Name of the author", example = "Socrates", required = true)
        String name,

        @Size(max = 1000, message = "Biography must not exceed 1000 characters")
        @Schema(description = "Biography of the author", example = "Ancient Greek philosopher...")
        String biography,

        @Min(value = -500, message = "Birth year must be at least -500")
        @Max(value = 2100, message = "Birth year must not exceed 2100")
        @Schema(description = "Birth year of the author", example = "-469")
        Integer birthYear,

        @Min(value = -500, message = "Death year must be at least -500")
        @Max(value = 2100, message = "Death year must not exceed 2100")
        @Schema(description = "Death year of the author", example = "-399")
        Integer deathYear,

        @Schema(description = "List of quotes by this author")
        List<QuoteDTO> quotes,

        @Schema(description = "Timestamp when the author was created")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp when the author was last updated")
        LocalDateTime updatedAt
) {

    /**
     * Constructor for creating a new author without ID (for creation requests)
     */
    public AuthorDTO(String name, String biography, Integer birthYear, Integer deathYear) {
        this(null, name, biography, birthYear, deathYear, null, null, null);
    }

    /**
     * Constructor for author without quotes (for simplified responses)
     */
    public AuthorDTO(Long id, String name, String biography, Integer birthYear, Integer deathYear,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, name, biography, birthYear, deathYear, null, createdAt, updatedAt);
    }
}
