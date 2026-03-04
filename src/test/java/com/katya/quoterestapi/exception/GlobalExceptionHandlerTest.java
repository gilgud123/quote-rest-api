package com.katya.quoterestapi.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.katya.quoterestapi.controller.AuthorController;
import com.katya.quoterestapi.service.AuthorService;
import com.katya.quoterestapi.service.QuoteService;

/** Unit tests for GlobalExceptionHandler */
@WebMvcTest(AuthorController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthorService authorService;

  @MockBean private QuoteService quoteService;

  @Test
  @DisplayName("Should return 404 when resource not found")
  void shouldReturn404WhenResourceNotFound() throws Exception {
    // Given
    when(authorService.getAuthorById(999L))
        .thenThrow(new ResourceNotFoundException("Author", 999L));

    // When & Then
    mockMvc
        .perform(get("/api/v1/authors/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Author not found with id: 999"))
        .andExpect(jsonPath("$.path").value("/api/v1/authors/999"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  @DisplayName("Should return 409 when resource already exists")
  void shouldReturn409WhenResourceAlreadyExists() throws Exception {
    // Given
    String requestBody =
        """
                {
                    "name": "Socrates",
                    "biography": "Ancient Greek philosopher",
                    "birthYear": -469,
                    "deathYear": -399
                }
                """;

    when(authorService.createAuthor(any()))
        .thenThrow(new ResourceAlreadyExistsException("Author", "name", "Socrates"));

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/authors").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message").value("Author already exists with name: Socrates"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  @DisplayName("Should return 400 when validation fails")
  void shouldReturn400WhenValidationFails() throws Exception {
    // Given
    String requestBody =
        """
                {
                    "name": "",
                    "birthYear": -600
                }
                """;

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/authors").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
        .andExpect(jsonPath("$.validationErrors").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  @DisplayName("Should return 400 when business validation fails")
  void shouldReturn400WhenBusinessValidationFails() throws Exception {
    // Given
    when(authorService.getAuthorById(1L))
        .thenThrow(new BusinessValidationException("Invalid business operation"));

    // When & Then
    mockMvc
        .perform(get("/api/v1/authors/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Invalid business operation"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  @DisplayName("Should return 400 for invalid parameter type")
  void shouldReturn400ForInvalidParameterType() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/v1/authors/invalid-id"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
