package com.katya.quoterestapi.controller;

import com.katya.quoterestapi.dto.AuthorDTO;
import com.katya.quoterestapi.dto.QuoteDTO;
import com.katya.quoterestapi.exception.ErrorResponse;
import com.katya.quoterestapi.service.AuthorService;
import com.katya.quoterestapi.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Author-related endpoints.
 * Provides CRUD operations with validation, pagination, and filtering.
 */
@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Authors", description = "API for managing authors")
public class AuthorController {

    private final AuthorService authorService;
    private final QuoteService quoteService;

    @Operation(summary = "Get all authors", description = "Retrieve a paginated list of all authors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved authors",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<AuthorDTO>> getAllAuthors(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field (e.g., 'name', 'birthYear')")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        log.info("GET /api/v1/authors - page: {}, size: {}, sortBy: {}, direction: {}", page, size, sortBy, direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AuthorDTO> authors = authorService.getAllAuthors(pageable);

        return ResponseEntity.ok(authors);
    }

    @Operation(summary = "Get author by ID", description = "Retrieve a specific author with all their quotes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved author",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(
            @Parameter(description = "Author ID", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/authors/{}", id);
        AuthorDTO author = authorService.getAuthorById(id);
        return ResponseEntity.ok(author);
    }

    @Operation(summary = "Search authors by name", description = "Search authors by name (case-insensitive partial match)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved authors",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<AuthorDTO>> searchAuthorsByName(
            @Parameter(description = "Name to search for", required = true)
            @RequestParam String name,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        log.info("GET /api/v1/authors/search?name={}", name);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AuthorDTO> authors = authorService.searchAuthorsByName(name, pageable);

        return ResponseEntity.ok(authors);
    }

    @Operation(summary = "Filter authors", description = "Filter authors by birth year or year range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved authors",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<AuthorDTO>> filterAuthors(
            @Parameter(description = "Birth year")
            @RequestParam(required = false) Integer birthYear,

            @Parameter(description = "Minimum birth year")
            @RequestParam(required = false) Integer minYear,

            @Parameter(description = "Maximum birth year")
            @RequestParam(required = false) Integer maxYear,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "birthYear") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        log.info("GET /api/v1/authors/filter - birthYear: {}, minYear: {}, maxYear: {}", birthYear, minYear, maxYear);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AuthorDTO> authors;

        if (birthYear != null) {
            authors = authorService.filterAuthorsByBirthYear(birthYear, pageable);
        } else if (minYear != null || maxYear != null) {
            authors = authorService.filterAuthorsByBirthYearRange(minYear, maxYear, pageable);
        } else {
            authors = authorService.getAllAuthors(pageable);
        }

        return ResponseEntity.ok(authors);
    }

    @Operation(summary = "Get quotes by author", description = "Retrieve all quotes by a specific author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quotes",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/quotes")
    public ResponseEntity<Page<QuoteDTO>> getQuotesByAuthor(
            @Parameter(description = "Author ID", required = true)
            @PathVariable Long id,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        log.info("GET /api/v1/authors/{}/quotes", id);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<QuoteDTO> quotes = quoteService.getQuotesByAuthorId(id, pageable);

        return ResponseEntity.ok(quotes);
    }

    @Operation(summary = "Get author statistics", description = "Get statistics for a specific author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getAuthorStatistics(
            @Parameter(description = "Author ID", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/authors/{}/stats", id);

        AuthorDTO author = authorService.getAuthorByIdWithoutQuotes(id);
        long quoteCount = authorService.getQuoteCountForAuthor(id);

        Map<String, Object> stats = new HashMap<>();
        stats.put("authorId", author.id());
        stats.put("authorName", author.name());
        stats.put("quoteCount", quoteCount);

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Create a new author", description = "Create a new author with validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Author created successfully",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Author with same name already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AuthorDTO> createAuthor(
            @Parameter(description = "Author data", required = true)
            @Valid @RequestBody AuthorDTO authorDTO
    ) {
        log.info("POST /api/v1/authors - name: {}", authorDTO.name());
        AuthorDTO createdAuthor = authorService.createAuthor(authorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }

    @Operation(summary = "Update an author", description = "Update an existing author (full update)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author updated successfully",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Author with same name already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(
            @Parameter(description = "Author ID", required = true)
            @PathVariable Long id,

            @Parameter(description = "Author data", required = true)
            @Valid @RequestBody AuthorDTO authorDTO
    ) {
        log.info("PUT /api/v1/authors/{}", id);
        AuthorDTO updatedAuthor = authorService.updateAuthor(id, authorDTO);
        return ResponseEntity.ok(updatedAuthor);
    }

    @Operation(summary = "Partially update an author", description = "Update specific fields of an author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author updated successfully",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Author with same name already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<AuthorDTO> patchAuthor(
            @Parameter(description = "Author ID", required = true)
            @PathVariable Long id,

            @Parameter(description = "Author data (partial)", required = true)
            @RequestBody AuthorDTO authorDTO
    ) {
        log.info("PATCH /api/v1/authors/{}", id);
        AuthorDTO updatedAuthor = authorService.patchAuthor(id, authorDTO);
        return ResponseEntity.ok(updatedAuthor);
    }

    @Operation(summary = "Delete an author", description = "Delete an author and all their quotes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "Author ID", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/authors/{}", id);
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
