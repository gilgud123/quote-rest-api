package com.katya.quoterestapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class AuthorControllerIntegrationTest {

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
    @DisplayName("GET /api/v1/authors returns paged authors")
    void shouldGetAllAuthors() throws Exception {
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[?(@.name=='Socrates')]").exists());
    }

    @Test
    @DisplayName("GET /api/v1/authors/{id} returns author with quotes")
    void shouldGetAuthorById() throws Exception {
        Long socratesId = authorIdByName("Socrates");

        mockMvc.perform(get("/api/v1/authors/{id}", socratesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Socrates"))
                .andExpect(jsonPath("$.quotes.length()").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/authors/search filters by name")
    void shouldSearchAuthorsByName() throws Exception {
        mockMvc.perform(get("/api/v1/authors/search")
                        .param("name", "pla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Plato"));
    }

    @Test
    @DisplayName("GET /api/v1/authors/filter filters by birth year range")
    void shouldFilterAuthorsByBirthYearRange() throws Exception {
        mockMvc.perform(get("/api/v1/authors/filter")
                        .param("minYear", "-470")
                        .param("maxYear", "-400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/authors/{id}/quotes returns quotes")
    void shouldGetQuotesByAuthor() throws Exception {
        Long socratesId = authorIdByName("Socrates");

        mockMvc.perform(get("/api/v1/authors/{id}/quotes", socratesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content.length()").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/authors/{id}/stats returns stats")
    void shouldGetAuthorStats() throws Exception {
        Long socratesId = authorIdByName("Socrates");

        mockMvc.perform(get("/api/v1/authors/{id}/stats", socratesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId").value(socratesId))
                .andExpect(jsonPath("$.authorName").value("Socrates"))
                .andExpect(jsonPath("$.quoteCount").value(4));
    }

    @Test
    @DisplayName("POST /api/v1/authors creates author")
    void shouldCreateAuthor() throws Exception {
        AuthorDTO request = new AuthorDTO("Epicurus", "Greek philosopher", -341, -270);

        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Epicurus"));

        assertThat(authorRepository.existsByName("Epicurus")).isTrue();
    }

    @Test
    @DisplayName("PUT /api/v1/authors/{id} updates author")
    void shouldUpdateAuthor() throws Exception {
        Long socratesId = authorIdByName("Socrates");
        AuthorDTO request = new AuthorDTO(null, "Socrates", "Updated biography", -469, -399, null, null);

        mockMvc.perform(put("/api/v1/authors/{id}", socratesId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.biography").value("Updated biography"));
    }

    @Test
    @DisplayName("DELETE /api/v1/authors/{id} deletes author")
    void shouldDeleteAuthor() throws Exception {
        Long platoId = authorIdByName("Plato");

        mockMvc.perform(delete("/api/v1/authors/{id}", platoId))
                .andExpect(status().isNoContent());

        assertThat(authorRepository.existsById(platoId)).isFalse();
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
