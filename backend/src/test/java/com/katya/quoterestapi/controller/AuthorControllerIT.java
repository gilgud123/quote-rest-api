package com.katya.quoterestapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.katya.quoterestapi.dto.AuthorDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
import com.katya.quoterestapi.repository.AuthorRepository;
import com.katya.quoterestapi.repository.QuoteRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"})
@DisplayName("AuthorController Integration Tests")
class AuthorControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private AuthorRepository authorRepository;

  @Autowired private QuoteRepository quoteRepository;

  @BeforeEach
  void setUp() {
    quoteRepository.deleteAll();
    authorRepository.deleteAll();
    seedData();
  }

  @AfterEach
  void tearDown() {
    quoteRepository.deleteAll();
    authorRepository.deleteAll();
  }

  @Nested
  @DisplayName("GET /api/v1/authors - Read Operations")
  class GetAllAuthors {

    @Test
    @DisplayName("Should return paged authors with default pagination")
    void getAllAuthors_withDefaults_returnsPagedAuthors() throws Exception {
      mockMvc
          .perform(get("/api/v1/authors"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(3))
          .andExpect(jsonPath("$.content.length()").value(3))
          .andExpect(jsonPath("$.content[?(@.name=='Socrates')]").exists());
    }

    @Test
    @DisplayName("Should return empty page when no authors exist")
    void getAllAuthors_whenNoAuthors_returnsEmptyPage() throws Exception {
      authorRepository.deleteAll();

      mockMvc
          .perform(get("/api/v1/authors"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(0))
          .andExpect(jsonPath("$.content.length()").value(0));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/authors/{id} - Get By ID")
  class GetAuthorById {

    @Test
    @DisplayName("Should return author with quotes when ID exists")
    void getAuthorById_whenExists_returnsAuthorWithQuotes() throws Exception {
      Long socratesId = authorIdByName("Socrates");

      mockMvc
          .perform(get("/api/v1/authors/{id}", socratesId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Socrates"))
          .andExpect(jsonPath("$.quotes.length()").value(4));
    }

    @Test
    @DisplayName("Should return 404 when author ID does not exist")
    void getAuthorById_whenNotExists_returns404() throws Exception {
      mockMvc
          .perform(get("/api/v1/authors/{id}", 99999L))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Author not found with id: 99999"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/authors/search - Search Operations")
  class SearchAuthors {

    @Test
    @DisplayName("Should search authors by name")
    void searchAuthors_byName_returnsMatchingAuthors() throws Exception {
      mockMvc
          .perform(get("/api/v1/authors/search").param("name", "pla"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(1))
          .andExpect(jsonPath("$.content[0].name").value("Plato"));
    }

    @Test
    @DisplayName("Should return empty results when no matches found")
    void searchAuthors_noMatches_returnsEmpty() throws Exception {
      mockMvc
          .perform(get("/api/v1/authors/search").param("name", "nonexistent"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(0));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/authors/filter - Filter Operations")
  class FilterAuthors {

    @Test
    @DisplayName("Should filter authors by birth year range")
    void filterAuthors_byBirthYearRange_returnsFiltered() throws Exception {
      mockMvc
          .perform(get("/api/v1/authors/filter").param("minYear", "-470").param("maxYear", "-400"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Should return all authors when no filters applied")
    void filterAuthors_noFilters_returnsAll() throws Exception {
      mockMvc
          .perform(get("/api/v1/authors/filter"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(3));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/authors/{id}/quotes - Author Quotes")
  class GetAuthorQuotes {

    @Test
    @DisplayName("Should return quotes for author")
    void getAuthorQuotes_whenExists_returnsQuotes() throws Exception {
      Long socratesId = authorIdByName("Socrates");

      mockMvc
          .perform(get("/api/v1/authors/{id}/quotes", socratesId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(4))
          .andExpect(jsonPath("$.content.length()").value(4));
    }

    @Test
    @DisplayName("Should return 404 when author does not exist")
    void getAuthorQuotes_whenNotExists_returns404() throws Exception {
      mockMvc.perform(get("/api/v1/authors/{id}/quotes", 99999L)).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/authors/{id}/stats - Author Stats")
  class GetAuthorStats {

    @Test
    @DisplayName("Should return stats for author")
    void getAuthorStats_whenExists_returnsStats() throws Exception {
      Long socratesId = authorIdByName("Socrates");

      mockMvc
          .perform(get("/api/v1/authors/{id}/stats", socratesId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.authorId").value(socratesId))
          .andExpect(jsonPath("$.authorName").value("Socrates"))
          .andExpect(jsonPath("$.quoteCount").value(4));
    }

    @Test
    @DisplayName("Should return 404 when author does not exist")
    void getAuthorStats_whenNotExists_returns404() throws Exception {
      mockMvc.perform(get("/api/v1/authors/{id}/stats", 99999L)).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/authors - Create Operations")
  class CreateAuthor {

    @Test
    @DisplayName("Should create author with valid data")
    void createAuthor_withValidData_returnsCreated() throws Exception {
      AuthorDTO request = new AuthorDTO("Epicurus", "Greek philosopher", -341, -270);

      mockMvc
          .perform(
              post("/api/v1/authors")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").isNumber())
          .andExpect(jsonPath("$.name").value("Epicurus"));

      assertThat(authorRepository.existsByName("Epicurus")).isTrue();
    }

    @Test
    @DisplayName("Should return 400 when name is null")
    void createAuthor_withNullName_returns400() throws Exception {
      AuthorDTO request = new AuthorDTO(null, "Greek philosopher", -341, -270);

      mockMvc
          .perform(
              post("/api/v1/authors")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when name is blank")
    void createAuthor_withBlankName_returns400() throws Exception {
      AuthorDTO request = new AuthorDTO("   ", "Greek philosopher", -341, -270);

      mockMvc
          .perform(
              post("/api/v1/authors")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/authors/{id} - Update Operations")
  class UpdateAuthor {

    @Test
    @DisplayName("Should update author with valid data")
    void updateAuthor_withValidData_returnsUpdated() throws Exception {
      Long socratesId = authorIdByName("Socrates");
      AuthorDTO request =
          new AuthorDTO(null, "Socrates", "Updated biography", -469, -399, null, null);

      mockMvc
          .perform(
              put("/api/v1/authors/{id}", socratesId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.biography").value("Updated biography"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent author")
    void updateAuthor_whenNotExists_returns404() throws Exception {
      AuthorDTO request = new AuthorDTO(null, "Test", "Bio", -400, -300, null, null);

      mockMvc
          .perform(
              put("/api/v1/authors/{id}", 99999L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when update data is invalid")
    void updateAuthor_withInvalidData_returns400() throws Exception {
      Long socratesId = authorIdByName("Socrates");
      AuthorDTO request = new AuthorDTO(null, "", "Bio", -400, -300, null, null);

      mockMvc
          .perform(
              put("/api/v1/authors/{id}", socratesId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/authors/{id} - Delete Operations")
  class DeleteAuthor {

    @Test
    @DisplayName("Should delete existing author")
    void deleteAuthor_whenExists_returnsNoContent() throws Exception {
      Long platoId = authorIdByName("Plato");

      mockMvc.perform(delete("/api/v1/authors/{id}", platoId)).andExpect(status().isNoContent());

      assertThat(authorRepository.existsById(platoId)).isFalse();
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent author")
    void deleteAuthor_whenNotExists_returns404() throws Exception {
      mockMvc.perform(delete("/api/v1/authors/{id}", 99999L)).andExpect(status().isNotFound());
    }
  }

  private Long authorIdByName(String name) {
    return authorRepository.findByName(name).orElseThrow().getId();
  }

  private Author createTestAuthor(String name, String biography, int birthYear, int deathYear) {
    return Author.builder()
        .name(name)
        .biography(biography)
        .birthYear(birthYear)
        .deathYear(deathYear)
        .build();
  }

  private void seedData() {
    Author socrates = createTestAuthor("Socrates", "Ancient Greek philosopher", -469, -399);
    socrates.addQuote(
        Quote.builder()
            .text("The unexamined life is not worth living.")
            .category("Philosophy")
            .build());
    socrates.addQuote(
        Quote.builder().text("I know that I know nothing.").category("Wisdom").build());
    socrates.addQuote(
        Quote.builder()
            .text("The only true wisdom is in knowing you know nothing.")
            .category("Wisdom")
            .build());
    socrates.addQuote(Quote.builder().text("By all means, marry.").category("Humor").build());

    Author plato = createTestAuthor("Plato", "Ancient Greek philosopher", -428, -348);
    plato.addQuote(
        Quote.builder()
            .text("Wise men speak because they have something to say.")
            .category("Wisdom")
            .build());
    plato.addQuote(
        Quote.builder()
            .text(
                "We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.")
            .category("Philosophy")
            .build());
    plato.addQuote(
        Quote.builder()
            .text(
                "One of the penalties for refusing to participate in politics is that you end up being governed by your inferiors.")
            .category("Politics")
            .build());

    Author aristotle =
        createTestAuthor("Aristotle", "Ancient Greek philosopher and polymath", -384, -322);
    aristotle.addQuote(
        Quote.builder()
            .text("Knowing yourself is the beginning of all wisdom.")
            .category("Wisdom")
            .build());
    aristotle.addQuote(
        Quote.builder()
            .text(
                "It is the mark of an educated mind to be able to entertain a thought without accepting it.")
            .category("Education")
            .build());
    aristotle.addQuote(
        Quote.builder()
            .text("We are what we repeatedly do. Excellence, then, is not an act, but a habit.")
            .category("Excellence")
            .build());

    authorRepository.saveAll(List.of(socrates, plato, aristotle));
  }
}
