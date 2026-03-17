package com.katya.quoterestapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.katya.quoterestapi.config.BaseIntegrationTest;
import com.katya.quoterestapi.dto.QuoteDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
import com.katya.quoterestapi.repository.AuthorRepository;
import com.katya.quoterestapi.repository.QuoteRepository;

@DisplayName("QuoteService Integration Tests")
class QuoteServiceIT extends BaseIntegrationTest {

  @Autowired private QuoteService quoteService;

  @Autowired private AuthorRepository authorRepository;

  @Autowired private QuoteRepository quoteRepository;

  private final Pageable pageable = PageRequest.of(0, 10);

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
  @DisplayName("Read Operations")
  class ReadOperations {

    @Test
    @DisplayName("Should get all quotes with pagination")
    void getAllQuotes_withPagination_returnsPagedQuotes() {
      Page<QuoteDTO> result = quoteService.getAllQuotes(pageable);

      assertThat(result.getTotalElements()).isEqualTo(10);
      assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Should get quotes by author ID")
    void getQuotesByAuthorId_whenExists_returnsAuthorQuotes() {
      Author plato = authorRepository.findByName("Plato").orElseThrow();

      Page<QuoteDTO> result = quoteService.getQuotesByAuthorId(plato.getId(), pageable);

      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getContent()).extracting(QuoteDTO::authorName).containsOnly("Plato");
    }

    @Test
    @DisplayName("Should return empty page when author has no quotes")
    void getQuotesByAuthorId_noQuotes_returnsEmptyPage() {
      Author newAuthor = createAndSaveAuthor("Epicurus", -341, -270);

      Page<QuoteDTO> result = quoteService.getQuotesByAuthorId(newAuthor.getId(), pageable);

      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("Search Operations")
  class SearchOperations {

    @Test
    @DisplayName("Should search quotes by text")
    void searchQuotesByText_withMatch_returnsMatchingQuotes() {
      Page<QuoteDTO> result = quoteService.searchQuotesByText("light", pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).text()).containsIgnoringCase("light");
    }

    @Test
    @DisplayName("Should return empty page when no matches found")
    void searchQuotesByText_noMatch_returnsEmpty() {
      Page<QuoteDTO> result = quoteService.searchQuotesByText("nonexistent", pageable);

      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("Filter Operations")
  class FilterOperations {

    @Test
    @DisplayName("Should filter quotes by category")
    void filterQuotesByCategory_withMatch_returnsFiltered() {
      Page<QuoteDTO> result = quoteService.filterQuotesByCategory("wisdom", pageable);

      assertThat(result.getTotalElements()).isEqualTo(4);
      assertThat(result.getContent()).extracting(QuoteDTO::category).containsOnly("Wisdom");
    }

    @Test
    @DisplayName("Should filter quotes with multiple criteria")
    void filterQuotes_withMultipleCriteria_returnsFiltered() {
      Author plato = authorRepository.findByName("Plato").orElseThrow();

      Page<QuoteDTO> result = quoteService.filterQuotes(plato.getId(), "Politics", null, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).category()).isEqualTo("Politics");
    }

    @Test
    @DisplayName("Should return empty page when filters match nothing")
    void filterQuotes_noMatches_returnsEmpty() {
      Page<QuoteDTO> result =
          quoteService.filterQuotes(null, "NonexistentCategory", null, pageable);

      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("Create/Update/Delete Operations")
  class WriteOperations {

    @Test
    @DisplayName("Should create quote with valid data")
    void createQuote_withValidData_returnsCreated() {
      Author socrates = authorRepository.findByName("Socrates").orElseThrow();
      QuoteDTO request =
          new QuoteDTO(
              "An unexamined mind is a dangerous thing.", null, "Philosophy", socrates.getId());

      QuoteDTO created = quoteService.createQuote(request);

      assertThat(created.id()).isNotNull();
      assertThat(created.text()).isEqualTo(request.text());
      assertThat(quoteRepository.existsById(created.id())).isTrue();
    }

    @Test
    @DisplayName("Should update quote with valid data")
    void updateQuote_withValidData_returnsUpdated() {
      Author socrates = authorRepository.findByName("Socrates").orElseThrow();
      QuoteDTO created =
          quoteService.createQuote(
              new QuoteDTO("Original text", null, "Philosophy", socrates.getId()));

      QuoteDTO updated =
          quoteService.updateQuote(
              created.id(), new QuoteDTO("Updated text", null, "Wisdom", socrates.getId()));

      assertThat(updated.text()).isEqualTo("Updated text");
      assertThat(updated.category()).isEqualTo("Wisdom");
    }

    @Test
    @DisplayName("Should delete existing quote")
    void deleteQuote_whenExists_deletesSuccessfully() {
      Author socrates = authorRepository.findByName("Socrates").orElseThrow();
      QuoteDTO created =
          quoteService.createQuote(
              new QuoteDTO("Test quote", null, "Philosophy", socrates.getId()));

      quoteService.deleteQuote(created.id());

      assertThat(quoteRepository.existsById(created.id())).isFalse();
    }
  }

  @Nested
  @DisplayName("Category Operations")
  class CategoryOperations {

    @Test
    @DisplayName("Should get all distinct categories sorted")
    void getAllCategories_returnsDistinctSorted() {
      List<String> categories = quoteService.getAllCategories();

      assertThat(categories)
          .containsExactly("Education", "Excellence", "Humor", "Philosophy", "Politics", "Wisdom");
    }

    @Test
    @DisplayName("Should return empty list when no quotes exist")
    void getAllCategories_noQuotes_returnsEmpty() {
      quoteRepository.deleteAll();

      List<String> categories = quoteService.getAllCategories();

      assertThat(categories).isEmpty();
    }
  }

  private Author createAndSaveAuthor(String name, int birthYear, int deathYear) {
    Author author =
        Author.builder()
            .name(name)
            .biography("Ancient philosopher")
            .birthYear(birthYear)
            .deathYear(deathYear)
            .build();
    return authorRepository.save(author);
  }

  private void seedData() {
    Author socrates =
        Author.builder()
            .name("Socrates")
            .biography("Ancient Greek philosopher")
            .birthYear(-469)
            .deathYear(-399)
            .build();
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

    Author plato =
        Author.builder()
            .name("Plato")
            .biography("Ancient Greek philosopher")
            .birthYear(-428)
            .deathYear(-348)
            .build();
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
        Author.builder()
            .name("Aristotle")
            .biography("Ancient Greek philosopher and polymath")
            .birthYear(-384)
            .deathYear(-322)
            .build();
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
