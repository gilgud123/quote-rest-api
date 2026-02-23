package com.katya.quoterestapi.controller;

import com.katya.quoterestapi.dto.QuoteDTO;
import com.katya.quoterestapi.exception.ErrorResponse;
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

import java.util.List;

/**
 * REST Controller for Quote-related endpoints.
 * Provides CRUD operations with validation, pagination, and filtering.
 */
@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Quotes", description = "API for managing quotes")
public class QuoteController {

    private final QuoteService quoteService;

    @Operation(summary = "Get all quotes", description = "Retrieve a paginated list of all quotes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quotes",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<QuoteDTO>> getAllQuotes(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field (e.g., 'text', 'category', 'createdAt')")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        log.info("GET /api/v1/quotes - page: {}, size: {}, sortBy: {}, direction: {}", page, size, sortBy, direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<QuoteDTO> quotes = quoteService.getAllQuotes(pageable);

        return ResponseEntity.ok(quotes);
    }

    @Operation(summary = "Get quote by ID", description = "Retrieve a specific quote by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quote",
                    content = @Content(schema = @Schema(implementation = QuoteDTO.class))),
            @ApiResponse(responseCode = "404", description = "Quote not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<QuoteDTO> getQuoteById(
            @Parameter(description = "Quote ID", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/quotes/{}", id);
        QuoteDTO quote = quoteService.getQuoteById(id);
        return ResponseEntity.ok(quote);
    }

    @Operation(summary = "Search quotes", description = "Search quotes by text or author name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quotes",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<QuoteDTO>> searchQuotes(
            @Parameter(description = "Search term (searches in text and author name)")
            @RequestParam(required = false) String q,

            @Parameter(description = "Search in quote text only")
            @RequestParam(required = false) String text,

            @Parameter(description = "Search by author name")
            @RequestParam(required = false) String author,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        log.info("GET /api/v1/quotes/search - q: {}, text: {}, author: {}", q, text, author);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<QuoteDTO> quotes;

        if (q != null && !q.isBlank()) {
            quotes = quoteService.searchQuotes(q, pageable);
        } else if (text != null && !text.isBlank()) {
            quotes = quoteService.searchQuotesByText(text, pageable);
        } else if (author != null && !author.isBlank()) {
            quotes = quoteService.searchQuotesByAuthorName(author, pageable);
        } else {
            quotes = quoteService.getAllQuotes(pageable);
        }

        return ResponseEntity.ok(quotes);
    }

    @Operation(summary = "Filter quotes", description = "Filter quotes by multiple criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quotes",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Author not found (if authorId is provided)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<QuoteDTO>> filterQuotes(
            @Parameter(description = "Filter by author ID")
            @RequestParam(required = false) Long authorId,

            @Parameter(description = "Filter by category")
            @RequestParam(required = false) String category,

            @Parameter(description = "Search term in quote text")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        log.info("GET /api/v1/quotes/filter - authorId: {}, category: {}, searchTerm: {}",
                authorId, category, searchTerm);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<QuoteDTO> quotes = quoteService.filterQuotes(authorId, category, searchTerm, pageable);

        return ResponseEntity.ok(quotes);
    }

    @Operation(summary = "Get all categories", description = "Retrieve a list of all distinct quote categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    })
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("GET /api/v1/quotes/categories");
        List<String> categories = quoteService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Create a new quote", description = "Create a new quote with validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quote created successfully",
                    content = @Content(schema = @Schema(implementation = QuoteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<QuoteDTO> createQuote(
            @Parameter(description = "Quote data", required = true)
            @Valid @RequestBody QuoteDTO quoteDTO
    ) {
        log.info("POST /api/v1/quotes - authorId: {}", quoteDTO.authorId());
        QuoteDTO createdQuote = quoteService.createQuote(quoteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuote);
    }

    @Operation(summary = "Update a quote", description = "Update an existing quote (full update)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quote updated successfully",
                    content = @Content(schema = @Schema(implementation = QuoteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quote or Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<QuoteDTO> updateQuote(
            @Parameter(description = "Quote ID", required = true)
            @PathVariable Long id,

            @Parameter(description = "Quote data", required = true)
            @Valid @RequestBody QuoteDTO quoteDTO
    ) {
        log.info("PUT /api/v1/quotes/{}", id);
        QuoteDTO updatedQuote = quoteService.updateQuote(id, quoteDTO);
        return ResponseEntity.ok(updatedQuote);
    }

    @Operation(summary = "Partially update a quote", description = "Update specific fields of a quote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quote updated successfully",
                    content = @Content(schema = @Schema(implementation = QuoteDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quote or Author not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<QuoteDTO> patchQuote(
            @Parameter(description = "Quote ID", required = true)
            @PathVariable Long id,

            @Parameter(description = "Quote data (partial)", required = true)
            @RequestBody QuoteDTO quoteDTO
    ) {
        log.info("PATCH /api/v1/quotes/{}", id);
        QuoteDTO updatedQuote = quoteService.patchQuote(id, quoteDTO);
        return ResponseEntity.ok(updatedQuote);
    }

    @Operation(summary = "Delete a quote", description = "Delete a quote by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quote deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Quote not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(
            @Parameter(description = "Quote ID", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/quotes/{}", id);
        quoteService.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }
}
