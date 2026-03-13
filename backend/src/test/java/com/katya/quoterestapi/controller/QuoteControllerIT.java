package com.katya.quoterestapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import com.katya.quoterestapi.dto.QuoteDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
import com.katya.quoterestapi.repository.AuthorRepository;
import com.katya.quoterestapi.repository.QuoteRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"})
@DisplayName("QuoteController Integration Tests")
class QuoteControllerIT {

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
  @DisplayName("GET /api/v1/quotes - Read Operations")
  class GetAllQuotes {

    @Test
    @DisplayName("Should return paged quotes with default pagination")
    void getAllQuotes_withDefaults_returnsPagedQuotes() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.totalElements").value(10))
          .andExpect(jsonPath("$.content.length()").value(10))
          .andExpect(jsonPath("$.content[0].id").isNumber())
          .andExpect(jsonPath("$.content[0].text").isString())
          .andExpect(jsonPath("$.content[0].authorName").isString());
    }

    @Test
    @DisplayName("Should return empty page when no quotes exist")
    void getAllQuotes_whenNoQuotes_returnsEmptyPage() throws Exception {
      quoteRepository.deleteAll();

      mockMvc
          .perform(get("/api/v1/quotes"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(0))
          .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Should apply pagination parameters correctly")
    void getAllQuotes_withPagination_appliesCorrectly() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes").param("page", "0").param("size", "5"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.size").value(5))
          .andExpect(jsonPath("$.content.length()").value(5));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quotes/{id} - Get By ID")
  class GetQuoteById {

    @Test
    @DisplayName("Should return quote when ID exists")
    void getQuoteById_whenExists_returnsQuote() throws Exception {
      Long quoteId = quoteRepository.findAll().get(0).getId();

      mockMvc
          .perform(get("/api/v1/quotes/{id}", quoteId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(quoteId))
          .andExpect(jsonPath("$.text").isString())
          .andExpect(jsonPath("$.category").isString())
          .andExpect(jsonPath("$.authorName").isString());
    }

    @Test
    @DisplayName("Should return 404 when quote ID does not exist")
    void getQuoteById_whenNotExists_returns404() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes/{id}", 99999L))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Quote not found with id: 99999"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quotes/search - Search Operations")
  class SearchQuotes {

    @Test
    @DisplayName("Should search quotes by text term")
    void searchQuotes_byText_returnsMatchingQuotes() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes/search").param("q", "light"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(1))
          .andExpect(
              jsonPath("$.content[0].text")
                  .value(org.hamcrest.Matchers.containsStringIgnoringCase("light")));
    }

    @Test
    @DisplayName("Should search quotes by author name")
    void searchQuotes_byAuthorName_returnsMatchingQuotes() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes/search").param("author", "pla"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(3))
          .andExpect(jsonPath("$.content[0].authorName").value("Plato"));
    }

    @Test
    @DisplayName("Should return empty results when no matches found")
    void searchQuotes_noMatches_returnsEmpty() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes/search").param("q", "nonexistent term xyz"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(0))
          .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Should handle empty search term gracefully")
    void searchQuotes_emptyTerm_returnsAllQuotes() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes/search").param("q", ""))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(10));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quotes/filter - Filter Operations")
  class FilterQuotes {

    @Test
    @DisplayName("Should filter quotes by author and category")
    void filterQuotes_withMultipleCriteria_returnsFiltered() throws Exception {
      Long platoId = authorIdByName("Plato");

      mockMvc
          .perform(
              get("/api/v1/quotes/filter")
                  .param("authorId", platoId.toString())
                  .param("category", "Politics"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(1))
          .andExpect(jsonPath("$.content[0].category").value("Politics"))
          .andExpect(jsonPath("$.content[0].authorName").value("Plato"));
    }

    @Test
    @DisplayName("Should return 404 when invalid author ID provided")
    void filterQuotes_invalidAuthorId_returns404() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes/filter").param("authorId", "99999"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Author not found with id: 99999"));
    }

    @Test
    @DisplayName("Should filter by category only")
    void filterQuotes_byCategoryOnly_returnsFiltered() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes/filter").param("category", "Wisdom"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(4))
          .andExpect(
              jsonPath("$.content[*].category")
                  .value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("Wisdom"))));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quotes/categories - Get Categories")
  class GetCategories {

    @Test
    @DisplayName("Should return distinct categories sorted alphabetically")
    void getAllCategories_returnsDistinctSorted() throws Exception {
      mockMvc
          .perform(get("/api/v1/quotes/categories"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[0]").value("Education"))
          .andExpect(jsonPath("$[5]").value("Wisdom"));
    }

    @Test
    @DisplayName("Should return empty array when no quotes exist")
    void getAllCategories_noQuotes_returnsEmpty() throws Exception {
      quoteRepository.deleteAll();

      mockMvc
          .perform(get("/api/v1/quotes/categories"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/quotes - Create Operations")
  class CreateQuote {

    @Test
    @DisplayName("Should create quote with valid data")
    void createQuote_withValidData_returnsCreated() throws Exception {
      Long socratesId = authorIdByName("Socrates");
      QuoteDTO request =
          new QuoteDTO("An unexamined mind is a dangerous thing.", null, "Philosophy", socratesId);

      mockMvc
          .perform(
              post("/api/v1/quotes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").isNumber())
          .andExpect(jsonPath("$.text").value("An unexamined mind is a dangerous thing."))
          .andExpect(jsonPath("$.category").value("Philosophy"))
          .andExpect(jsonPath("$.authorId").value(socratesId))
          .andExpect(jsonPath("$.authorName").value("Socrates"));

      assertThat(quoteRepository.count()).isEqualTo(11);
    }

    @Test
    @DisplayName("Should return 400 when text is null")
    void createQuote_withNullText_returns400() throws Exception {
      Long socratesId = authorIdByName("Socrates");
      QuoteDTO request = new QuoteDTO(null, null, "Philosophy", socratesId);

      mockMvc
          .perform(
              post("/api/v1/quotes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when text is blank")
    void createQuote_withBlankText_returns400() throws Exception {
      Long socratesId = authorIdByName("Socrates");
      QuoteDTO request = new QuoteDTO("   ", null, "Philosophy", socratesId);

      mockMvc
          .perform(
              post("/api/v1/quotes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when author does not exist")
    void createQuote_withNonExistentAuthor_returns404() throws Exception {
      QuoteDTO request = new QuoteDTO("Test quote", null, "Philosophy", 99999L);

      mockMvc
          .perform(
              post("/api/v1/quotes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when author ID is null")
    void createQuote_withNullAuthorId_returns400() throws Exception {
      QuoteDTO request = new QuoteDTO("Test quote", null, "Philosophy", null);

      mockMvc
          .perform(
              post("/api/v1/quotes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/quotes/{id} - Update Operations")
  class UpdateQuote {

    @Test
    @DisplayName("Should update quote with valid data")
    void updateQuote_withValidData_returnsUpdated() throws Exception {
      Long quoteId = quoteRepository.findAll().get(0).getId();
      Long socratesId = authorIdByName("Socrates");
      QuoteDTO request =
          new QuoteDTO("An examined mind is a powerful thing.", null, "Wisdom", socratesId);

      mockMvc
          .perform(
              put("/api/v1/quotes/{id}", quoteId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(quoteId))
          .andExpect(jsonPath("$.text").value("An examined mind is a powerful thing."))
          .andExpect(jsonPath("$.category").value("Wisdom"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent quote")
    void updateQuote_whenNotExists_returns404() throws Exception {
      Long socratesId = authorIdByName("Socrates");
      QuoteDTO request =
          new QuoteDTO("Valid test quote with enough characters", null, "Philosophy", socratesId);

      mockMvc
          .perform(
              put("/api/v1/quotes/{id}", 99999L)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when update data is invalid")
    void updateQuote_withInvalidData_returns400() throws Exception {
      Long quoteId = quoteRepository.findAll().get(0).getId();
      QuoteDTO request = new QuoteDTO("", null, "Philosophy", null);

      mockMvc
          .perform(
              put("/api/v1/quotes/{id}", quoteId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/quotes/{id} - Delete Operations")
  class DeleteQuote {

    @Test
    @DisplayName("Should delete existing quote")
    void deleteQuote_whenExists_returnsNoContent() throws Exception {
      Long quoteId = quoteRepository.findAll().get(0).getId();
      long initialCount = quoteRepository.count();

      mockMvc.perform(delete("/api/v1/quotes/{id}", quoteId)).andExpect(status().isNoContent());

      assertThat(quoteRepository.existsById(quoteId)).isFalse();
      assertThat(quoteRepository.count()).isEqualTo(initialCount - 1);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent quote")
    void deleteQuote_whenNotExists_returns404() throws Exception {
      mockMvc.perform(delete("/api/v1/quotes/{id}", 99999L)).andExpect(status().isNotFound());
    }
  }

  private Long authorIdByName(String name) {
    return authorRepository.findByName(name).orElseThrow().getId();
  }

  private void seedData() {
    Author socrates = createSocrates();
    Author plato = createPlato();
    Author aristotle = createAristotle();
    authorRepository.saveAll(List.of(socrates, plato, aristotle));
  }

  private Author createSocrates() {
    Author author =
        Author.builder()
            .name("Socrates")
            .biography("Ancient Greek philosopher")
            .birthYear(-469)
            .deathYear(-399)
            .build();
    author.addQuote(
        Quote.builder()
            .text("The unexamined life is not worth living.")
            .category("Philosophy")
            .build());
    author.addQuote(Quote.builder().text("I know that I know nothing.").category("Wisdom").build());
    author.addQuote(
        Quote.builder()
            .text("The only true wisdom is in knowing you know nothing.")
            .category("Wisdom")
            .build());
    author.addQuote(Quote.builder().text("By all means, marry.").category("Humor").build());
    return author;
  }

  private Author createPlato() {
    Author author =
        Author.builder()
            .name("Plato")
            .biography("Ancient Greek philosopher")
            .birthYear(-428)
            .deathYear(-348)
            .build();
    author.addQuote(
        Quote.builder()
            .text("Wise men speak because they have something to say.")
            .category("Wisdom")
            .build());
    author.addQuote(
        Quote.builder()
            .text(
                "We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.")
            .category("Philosophy")
            .build());
    author.addQuote(
        Quote.builder()
            .text(
                "One of the penalties for refusing to participate in politics is that you end up being governed by your inferiors.")
            .category("Politics")
            .build());
    return author;
  }

  private Author createAristotle() {
    Author author =
        Author.builder()
            .name("Aristotle")
            .biography("Ancient Greek philosopher and polymath")
            .birthYear(-384)
            .deathYear(-322)
            .build();
    author.addQuote(
        Quote.builder()
            .text("Knowing yourself is the beginning of all wisdom.")
            .category("Wisdom")
            .build());
    author.addQuote(
        Quote.builder()
            .text(
                "It is the mark of an educated mind to be able to entertain a thought without accepting it.")
            .category("Education")
            .build());
    author.addQuote(
        Quote.builder()
            .text("We are what we repeatedly do. Excellence, then, is not an act, but a habit.")
            .category("Excellence")
            .build());
    return author;
  }
}
