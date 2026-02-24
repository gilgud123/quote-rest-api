package com.katya.quoterestapi.service;

import com.katya.quoterestapi.dto.QuoteDTO;
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
class QuoteServiceIntegrationTest {

    @Autowired
    private QuoteService quoteService;

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
    @DisplayName("Should get all quotes with pagination")
    void shouldGetAllQuotes() {
        Page<QuoteDTO> result = quoteService.getAllQuotes(pageable);

        assertThat(result.getTotalElements()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should get quotes by author ID")
    void shouldGetQuotesByAuthorId() {
        Author plato = authorRepository.findByName("Plato").orElseThrow();

        Page<QuoteDTO> result = quoteService.getQuotesByAuthorId(plato.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting(QuoteDTO::authorName)
                .containsOnly("Plato");
    }

    @Test
    @DisplayName("Should search quotes by text")
    void shouldSearchQuotesByText() {
        Page<QuoteDTO> result = quoteService.searchQuotesByText("light", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).text()).containsIgnoringCase("light");
    }

    @Test
    @DisplayName("Should filter quotes by category")
    void shouldFilterQuotesByCategory() {
        Page<QuoteDTO> result = quoteService.filterQuotesByCategory("wisdom", pageable);

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent())
                .extracting(QuoteDTO::category)
                .containsOnly("Wisdom");
    }

    @Test
    @DisplayName("Should filter quotes with multiple criteria")
    void shouldFilterQuotes() {
        Author plato = authorRepository.findByName("Plato").orElseThrow();

        Page<QuoteDTO> result = quoteService.filterQuotes(plato.getId(), "Politics", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).category()).isEqualTo("Politics");
    }

    @Test
    @DisplayName("Should create, update, and delete a quote")
    void shouldCreateUpdateDeleteQuote() {
        Author socrates = authorRepository.findByName("Socrates").orElseThrow();

        QuoteDTO created = quoteService.createQuote(
                new QuoteDTO("An unexamined mind is a dangerous thing.", null, "Philosophy", socrates.getId())
        );

        QuoteDTO updated = quoteService.updateQuote(
                created.id(),
                new QuoteDTO("An examined mind is a powerful thing.", null, "Wisdom", socrates.getId())
        );

        quoteService.deleteQuote(updated.id());

        assertThat(quoteRepository.existsById(updated.id())).isFalse();
    }

    @Test
    @DisplayName("Should get all distinct categories")
    void shouldGetAllCategories() {
        List<String> categories = quoteService.getAllCategories();

        assertThat(categories).containsExactly(
                "Education",
                "Excellence",
                "Humor",
                "Philosophy",
                "Politics",
                "Wisdom"
        );
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
