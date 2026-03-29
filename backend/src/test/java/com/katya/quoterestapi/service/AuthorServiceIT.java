package com.katya.quoterestapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.katya.quoterestapi.dto.AuthorDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
import com.katya.quoterestapi.repository.AuthorRepository;
import com.katya.quoterestapi.repository.QuoteRepository;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"})
@DisplayName("AuthorService Integration Tests")
class AuthorServiceIT {

  @Autowired private AuthorService authorService;

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
    @DisplayName("Should get all authors with pagination")
    void getAllAuthors_withPagination_returnsPagedAuthors() {
      Page<AuthorDTO> result = authorService.getAllAuthors(pageable);

      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getContent())
          .extracting(AuthorDTO::name)
          .containsExactlyInAnyOrder("Socrates", "Plato", "Aristotle");
    }

    @Test
    @DisplayName("Should return empty page when no authors exist")
    void getAllAuthors_noAuthors_returnsEmptyPage() {
      authorRepository.deleteAll();

      Page<AuthorDTO> result = authorService.getAllAuthors(pageable);

      assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should get author by ID with quotes")
    void getAuthorById_whenExists_returnsAuthorWithQuotes() {
      Author aristotle = authorRepository.findByName("Aristotle").orElseThrow();

      AuthorDTO result = authorService.getAuthorById(aristotle.getId());

      assertThat(result.name()).isEqualTo("Aristotle");
      assertThat(result.quotes()).isNotNull();
      assertThat(result.quotes()).hasSize(4);
    }
  }

  @Nested
  @DisplayName("Search Operations")
  class SearchOperations {

    @Test
    @DisplayName("Should search authors by name")
    void searchAuthorsByName_withMatch_returnsMatchingAuthors() {
      Page<AuthorDTO> result = authorService.searchAuthorsByName("pla", pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).name()).isEqualTo("Plato");
    }

    @Test
    @DisplayName("Should return empty page when no matches found")
    void searchAuthorsByName_noMatch_returnsEmpty() {
      Page<AuthorDTO> result = authorService.searchAuthorsByName("nonexistent", pageable);

      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("Filter Operations")
  class FilterOperations {

    @Test
    @DisplayName("Should filter authors by birth year range")
    void filterAuthorsByBirthYearRange_withRange_returnsFiltered() {
      Page<AuthorDTO> result = authorService.filterAuthorsByBirthYearRange(-470, -400, pageable);

      assertThat(result.getTotalElements()).isEqualTo(2);
      assertThat(result.getContent())
          .extracting(AuthorDTO::name)
          .containsExactlyInAnyOrder("Socrates", "Plato");
    }

    @Test
    @DisplayName("Should return empty page when no authors in range")
    void filterAuthorsByBirthYearRange_noMatch_returnsEmpty() {
      Page<AuthorDTO> result = authorService.filterAuthorsByBirthYearRange(-100, -50, pageable);

      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("Create/Update/Delete Operations")
  class WriteOperations {

    @Test
    @DisplayName("Should create author with valid data")
    void createAuthor_withValidData_returnsCreated() {
      AuthorDTO request = new AuthorDTO("Epicurus", "Greek philosopher", -341, -270);

      AuthorDTO created = authorService.createAuthor(request);

      assertThat(created.id()).isNotNull();
      assertThat(created.name()).isEqualTo("Epicurus");
      assertThat(authorRepository.existsByName("Epicurus")).isTrue();
    }

    @Test
    @DisplayName("Should update author with valid data")
    void updateAuthor_withValidData_returnsUpdated() {
      Author socrates = authorRepository.findByName("Socrates").orElseThrow();
      AuthorDTO request =
          new AuthorDTO(null, "Socrates", "Updated biography", -469, -399, null, null);

      AuthorDTO updated = authorService.updateAuthor(socrates.getId(), request);

      assertThat(updated.biography()).isEqualTo("Updated biography");
    }

    @Test
    @DisplayName("Should delete existing author")
    void deleteAuthor_whenExists_deletesSuccessfully() {
      Author plato = authorRepository.findByName("Plato").orElseThrow();

      authorService.deleteAuthor(plato.getId());

      assertThat(authorRepository.existsById(plato.getId())).isFalse();
    }
  }

  @Nested
  @DisplayName("Stats Operations")
  class StatsOperations {

    @Test
    @DisplayName("Should get quote count for author")
    void getQuoteCountForAuthor_whenExists_returnsCount() {
      Author socrates = authorRepository.findByName("Socrates").orElseThrow();

      long count = authorService.getQuoteCountForAuthor(socrates.getId());

      assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("Should return zero count for author with no quotes")
    void getQuoteCountForAuthor_noQuotes_returnsZero() {
      Author newAuthor = createAndSaveAuthor("Epicurus", -341, -270);

      long count = authorService.getQuoteCountForAuthor(newAuthor.getId());

      assertThat(count).isZero();
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
