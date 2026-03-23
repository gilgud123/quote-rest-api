package com.katya.quoterestapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"})
class AuthorRepositoryTest {

  @Autowired private AuthorRepository authorRepository;

  @Autowired private QuoteRepository quoteRepository;

  private final Pageable pageable = PageRequest.of(0, 10);

  @BeforeEach
  void setUp() {
    quoteRepository.deleteAll();
    authorRepository.deleteAll();

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

    authorRepository.saveAll(List.of(socrates, plato, aristotle));
  }

  @AfterEach
  void tearDown() {
    quoteRepository.deleteAll();
    authorRepository.deleteAll();
  }

  @Test
  @DisplayName("Should find authors by name containing ignore case")
  void shouldFindByNameContainingIgnoreCase() {
    Page<Author> result = authorRepository.findByNameContainingIgnoreCase("socr", pageable);

    assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1);
    assertThat(result.getContent().get(0).getName()).containsIgnoringCase("socr");
  }

  @Test
  @DisplayName("Should find authors by birth year")
  void shouldFindByBirthYear() {
    Page<Author> result = authorRepository.findByBirthYear(-384, pageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Aristotle");
  }

  @Test
  @DisplayName("Should find authors by birth year range")
  void shouldFindByBirthYearBetween() {
    Page<Author> result = authorRepository.findByBirthYearBetween(null, -350, pageable);

    assertThat(result.getTotalElements()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should find authors by birth year range with min only")
  void shouldFindByBirthYearBetweenMinOnly() {
    Page<Author> result = authorRepository.findByBirthYearBetween(-400, null, pageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Aristotle");
  }

  @Test
  @DisplayName("Should find authors by birth year range with max only")
  void shouldFindByBirthYearBetweenMaxOnly() {
    Page<Author> result = authorRepository.findByBirthYearBetween(null, -420, pageable);

    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent())
        .extracting(Author::getName)
        .containsExactlyInAnyOrder("Socrates", "Plato");
  }

  @Test
  @DisplayName("Should return empty when author name not found")
  void shouldReturnEmptyWhenNameNotFound() {
    assertThat(authorRepository.findByName("Unknown Author")).isEmpty();
  }

  @Test
  @DisplayName("Should check author exists by name")
  void shouldCheckExistsByName() {
    boolean exists = authorRepository.existsByName("Socrates");
    boolean notExists = authorRepository.existsByName("Unknown Author");

    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should count quotes by author id")
  void shouldCountQuotesByAuthorId() {
    Author author = authorRepository.findByName("Socrates").orElseThrow();

    long count = authorRepository.countQuotesByAuthorId(author.getId());

    assertThat(count).isEqualTo(4);
  }

  @Test
  @DisplayName("Should find author by exact name")
  void shouldFindByExactName() {
    Author author = authorRepository.findByName("Plato").orElseThrow();

    assertThat(author.getName()).isEqualTo("Plato");
    assertThat(author.getBirthYear()).isEqualTo(-428);
  }

  @Test
  @DisplayName("Should return all authors when birth year range has no bounds")
  void shouldReturnAllAuthorsWhenRangeIsNull() {
    Page<Author> result = authorRepository.findByBirthYearBetween(null, null, pageable);

    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getContent())
        .extracting(Author::getName)
        .containsExactlyInAnyOrder("Socrates", "Plato", "Aristotle");
  }
}
