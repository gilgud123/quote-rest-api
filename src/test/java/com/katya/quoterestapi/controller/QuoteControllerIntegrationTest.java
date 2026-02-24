package com.katya.quoterestapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class QuoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private QuoteRepository quoteRepository;

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
    @DisplayName("GET /api/v1/quotes returns paged quotes")
    void shouldGetAllQuotes() throws Exception {
        mockMvc.perform(get("/api/v1/quotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.content.length()").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/quotes/{id} returns quote")
    void shouldGetQuoteById() throws Exception {
        Long quoteId = quoteRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/quotes/{id}", quoteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(quoteId));
    }

    @Test
    @DisplayName("GET /api/v1/quotes/search searches by term")
    void shouldSearchQuotes() throws Exception {
        mockMvc.perform(get("/api/v1/quotes/search")
                        .param("q", "light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/quotes/search searches by author name")
    void shouldSearchQuotesByAuthorName() throws Exception {
        mockMvc.perform(get("/api/v1/quotes/search")
                        .param("author", "pla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].authorName").value("Plato"));
    }

    @Test
    @DisplayName("GET /api/v1/quotes/filter filters by criteria")
    void shouldFilterQuotes() throws Exception {
        Long platoId = authorIdByName("Plato");

        mockMvc.perform(get("/api/v1/quotes/filter")
                        .param("authorId", platoId.toString())
                        .param("category", "Politics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].category").value("Politics"));
    }

    @Test
    @DisplayName("GET /api/v1/quotes/categories returns distinct categories")
    void shouldGetAllCategories() throws Exception {
        mockMvc.perform(get("/api/v1/quotes/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Education"))
                .andExpect(jsonPath("$[5]").value("Wisdom"));
    }

    @Test
    @DisplayName("POST /api/v1/quotes creates quote")
    void shouldCreateQuote() throws Exception {
        Long socratesId = authorIdByName("Socrates");
        QuoteDTO request = new QuoteDTO(
                "An unexamined mind is a dangerous thing.",
                null,
                "Philosophy",
                socratesId
        );

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.authorId").value(socratesId));

        assertThat(quoteRepository.findAll().size()).isEqualTo(11);
    }

    @Test
    @DisplayName("PUT /api/v1/quotes/{id} updates quote")
    void shouldUpdateQuote() throws Exception {
        Long quoteId = quoteRepository.findAll().get(0).getId();
        Long socratesId = authorIdByName("Socrates");
        QuoteDTO request = new QuoteDTO(
                "An examined mind is a powerful thing.",
                null,
                "Wisdom",
                socratesId
        );

        mockMvc.perform(put("/api/v1/quotes/{id}", quoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Wisdom"));
    }

    @Test
    @DisplayName("DELETE /api/v1/quotes/{id} deletes quote")
    void shouldDeleteQuote() throws Exception {
        Long quoteId = quoteRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/v1/quotes/{id}", quoteId))
                .andExpect(status().isNoContent());

        assertThat(quoteRepository.existsById(quoteId)).isFalse();
    }

    private Long authorIdByName(String name) {
        return authorRepository.findByName(name).orElseThrow().getId();
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
