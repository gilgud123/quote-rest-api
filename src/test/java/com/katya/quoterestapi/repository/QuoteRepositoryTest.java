package com.katya.quoterestapi.repository;

import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.entity.Quote;
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
class QuoteRepositoryTest {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        quoteRepository.deleteAll();
        authorRepository.deleteAll();

        Author socrates = Author.builder()
                .name("Socrates")
                .biography("Ancient Greek philosopher")
                .birthYear(-469)
                .deathYear(-399)
                .build();
        socrates.addQuote(Quote.builder()
                .text("The unexamined life is not worth living.")
                .category("Philosophy")
                .build());
        socrates.addQuote(Quote.builder()
                .text("I know that I know nothing.")
                .category("Wisdom")
                .build());
        socrates.addQuote(Quote.builder()
                .text("The only true wisdom is in knowing you know nothing.")
                .category("Wisdom")
                .build());
        socrates.addQuote(Quote.builder()
                .text("By all means, marry.")
                .category("Humor")
                .build());

        Author plato = Author.builder()
                .name("Plato")
                .biography("Ancient Greek philosopher")
                .birthYear(-428)
                .deathYear(-348)
                .build();
        plato.addQuote(Quote.builder()
                .text("Wise men speak because they have something to say.")
                .category("Wisdom")
                .build());
        plato.addQuote(Quote.builder()
                .text("We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.")
                .category("Philosophy")
                .build());
        plato.addQuote(Quote.builder()
                .text("One of the penalties for refusing to participate in politics is that you end up being governed by your inferiors.")
                .category("Politics")
                .build());

        Author aristotle = Author.builder()
                .name("Aristotle")
                .biography("Ancient Greek philosopher and polymath")
                .birthYear(-384)
                .deathYear(-322)
                .build();
        aristotle.addQuote(Quote.builder()
                .text("Knowing yourself is the beginning of all wisdom.")
                .category("Wisdom")
                .build());
        aristotle.addQuote(Quote.builder()
                .text("It is the mark of an educated mind to be able to entertain a thought without accepting it.")
                .category("Education")
                .build());
        aristotle.addQuote(Quote.builder()
                .text("We are what we repeatedly do. Excellence, then, is not an act, but a habit.")
                .category("Excellence")
                .build());

        authorRepository.saveAll(List.of(socrates, plato, aristotle));
    }

    @AfterEach
    void tearDown() {
        quoteRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find quotes by author id")
    void shouldFindByAuthorId() {
        Author socrates = authorRepository.findByName("Socrates").orElseThrow();

        Page<Quote> result = quoteRepository.findByAuthorId(socrates.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent())
                .extracting(Quote::getText)
                .containsExactlyInAnyOrder(
                        "The unexamined life is not worth living.",
                        "I know that I know nothing.",
                        "The only true wisdom is in knowing you know nothing.",
                        "By all means, marry."
                );
    }

    @Test
    @DisplayName("Should find quotes by text containing ignore case")
    void shouldFindByTextContainingIgnoreCase() {
        Page<Quote> result = quoteRepository.findByTextContainingIgnoreCase("UNEXAMINED", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getText()).containsIgnoringCase("unexamined");
    }

    @Test
    @DisplayName("Should find quotes by category")
    void shouldFindByCategory() {
        Page<Quote> result = quoteRepository.findByCategory("Wisdom", pageable);

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).allMatch(quote -> "Wisdom".equals(quote.getCategory()));
    }

    @Test
    @DisplayName("Should find quotes by category ignoring case")
    void shouldFindByCategoryIgnoreCase() {
        Page<Quote> result = quoteRepository.findByCategoryIgnoreCase("philosophy", pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(quote -> "Philosophy".equals(quote.getCategory()));
    }

    @Test
    @DisplayName("Should find quotes by author name containing")
    void shouldFindByAuthorNameContaining() {
        Page<Quote> result = quoteRepository.findByAuthorNameContaining("pla", pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(Quote::getText)
                .containsExactlyInAnyOrder(
                        "Wise men speak because they have something to say.",
                        "We can easily forgive a child who is afraid of the dark; the real tragedy of life is when men are afraid of the light.",
                        "One of the penalties for refusing to participate in politics is that you end up being governed by your inferiors."
                );
    }

    @Test
    @DisplayName("Should search quotes by text or author name")
    void shouldSearchQuotes() {
        Page<Quote> result = quoteRepository.searchQuotes("light", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getText()).containsIgnoringCase("light");
    }

    @Test
    @DisplayName("Should find quotes with filters")
    void shouldFindWithFilters() {
        Author plato = authorRepository.findByName("Plato").orElseThrow();

        Page<Quote> result = quoteRepository.findWithFilters(plato.getId(), "Politics", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("Politics");
    }

    @Test
    @DisplayName("Should find all distinct categories")
    void shouldFindAllCategories() {
        List<String> categories = quoteRepository.findAllCategories();

        assertThat(categories).containsExactly(
                "Education",
                "Excellence",
                "Humor",
                "Philosophy",
                "Politics",
                "Wisdom"
        );
    }
}
