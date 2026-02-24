package com.katya.quoterestapi.service;

import com.katya.quoterestapi.dto.AuthorDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
import com.katya.quoterestapi.repository.AuthorRepository;
import com.katya.quoterestapi.repository.QuoteRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class AuthorServiceIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private QuoteRepository quoteRepository;

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

    @Test
    @DisplayName("Should get all authors with pagination")
    void shouldGetAllAuthors() {
        Page<AuthorDTO> result = authorService.getAllAuthors(pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(AuthorDTO::name)
                .containsExactlyInAnyOrder("Socrates", "Plato", "Aristotle");
    }

    @Test
    @DisplayName("Should get author by ID with quotes")
    void shouldGetAuthorById() {
        Author socrates = authorRepository.findByName("Socrates").orElseThrow();

        AuthorDTO result = authorService.getAuthorById(socrates.getId());

        assertThat(result.name()).isEqualTo("Socrates");
        assertThat(result.quotes()).isNotNull();
        assertThat(result.quotes()).hasSize(4);
    }

    @Test
    @DisplayName("Should search authors by name")
    void shouldSearchAuthorsByName() {
        Page<AuthorDTO> result = authorService.searchAuthorsByName("pla", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Plato");
    }

    @Test
    @DisplayName("Should filter authors by birth year range")
    void shouldFilterAuthorsByBirthYearRange() {
        Page<AuthorDTO> result = authorService.filterAuthorsByBirthYearRange(-470, -400, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(AuthorDTO::name)
                .containsExactlyInAnyOrder("Socrates", "Plato");
    }

    @Test
    @DisplayName("Should create a new author")
    void shouldCreateAuthor() {
        AuthorDTO created = authorService.createAuthor(
                new AuthorDTO("Epicurus", "Greek philosopher", -341, -270)
        );

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Epicurus");
        assertThat(authorRepository.existsByName("Epicurus")).isTrue();
    }

    @Test
    @DisplayName("Should update an existing author")
    void shouldUpdateAuthor() {
        Author socrates = authorRepository.findByName("Socrates").orElseThrow();

        AuthorDTO updated = authorService.updateAuthor(
                socrates.getId(),
                new AuthorDTO(null, "Socrates", "Updated biography", -469, -399, null, null)
        );

        assertThat(updated.biography()).isEqualTo("Updated biography");
    }

    @Test
    @DisplayName("Should delete an author")
    void shouldDeleteAuthor() {
        Author plato = authorRepository.findByName("Plato").orElseThrow();

        authorService.deleteAuthor(plato.getId());

        assertThat(authorRepository.existsById(plato.getId())).isFalse();
    }

    @Test
    @DisplayName("Should get quote count for author")
    void shouldGetQuoteCountForAuthor() {
        Author socrates = authorRepository.findByName("Socrates").orElseThrow();

        long count = authorService.getQuoteCountForAuthor(socrates.getId());

        assertThat(count).isEqualTo(4);
    }

    private void seedData() {
        Author socrates = Author.builder()
                .name("Socrates")
                .biography("Ancient Greek philosopher")
                .birthYear(-469)
                .deathYear(-399)
                .build();
        socrates.addQuote(Quote.builder().text("The unexamined life is not worth living.").category("Philosophy").build());
        socrates.addQuote(Quote.builder().text("I know that I know nothing.").category("Wisdom").build());
        socrates.addQuote(Quote.builder().text("The only true wisdom is in knowing you know nothing.").category("Wisdom").build());
        socrates.addQuote(Quote.builder().text("By all means, marry.").category("Humor").build());

        Author plato = Author.builder()
                .name("Plato")
                .biography("Ancient Greek philosopher")
                .birthYear(-428)
                .deathYear(-348)
                .build();
        plato.addQuote(Quote.builder().text("Wise men speak because they have something to say.").category("Wisdom").build());
        plato.addQuote(Quote.builder().text("We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.").category("Philosophy").build());
        plato.addQuote(Quote.builder().text("One of the penalties for refusing to participate in politics is that you end up being governed by your inferiors.").category("Politics").build());

        Author aristotle = Author.builder()
                .name("Aristotle")
                .biography("Ancient Greek philosopher and polymath")
                .birthYear(-384)
                .deathYear(-322)
                .build();
        aristotle.addQuote(Quote.builder().text("Knowing yourself is the beginning of all wisdom.").category("Wisdom").build());
        aristotle.addQuote(Quote.builder().text("It is the mark of an educated mind to be able to entertain a thought without accepting it.").category("Education").build());
        aristotle.addQuote(Quote.builder().text("We are what we repeatedly do. Excellence, then, is not an act, but a habit.").category("Excellence").build());

        authorRepository.saveAll(List.of(socrates, plato, aristotle));
    }
}
