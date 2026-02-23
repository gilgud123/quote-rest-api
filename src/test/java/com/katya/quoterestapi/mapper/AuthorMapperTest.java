package com.katya.quoterestapi.mapper;

import com.katya.quoterestapi.dto.AuthorDTO;
import com.katya.quoterestapi.entity.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuthorMapper - Pure unit tests without Spring context
 */
@DisplayName("AuthorMapper Unit Tests")
class AuthorMapperTest {

    private AuthorMapper authorMapper;

    private Author testAuthor;
    private AuthorDTO testAuthorDTO;

    @BeforeEach
    void setUp() {
        // Use MapStruct factory to get mapper instance for unit testing
        authorMapper = Mappers.getMapper(AuthorMapper.class);

        testAuthor = Author.builder()
                .id(1L)
                .name("Socrates")
                .biography("Ancient Greek philosopher")
                .birthYear(-469)
                .deathYear(-399)
                .quotes(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testAuthorDTO = new AuthorDTO(
                1L,
                "Socrates",
                "Ancient Greek philosopher",
                -469,
                -399,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should map Author entity to AuthorDTO without quotes")
    void shouldMapEntityToDtoWithoutQuotes() {
        // When
        AuthorDTO result = authorMapper.toDtoWithoutQuotes(testAuthor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testAuthor.getId());
        assertThat(result.name()).isEqualTo(testAuthor.getName());
        assertThat(result.biography()).isEqualTo(testAuthor.getBiography());
        assertThat(result.birthYear()).isEqualTo(testAuthor.getBirthYear());
        assertThat(result.deathYear()).isEqualTo(testAuthor.getDeathYear());
        assertThat(result.quotes()).isNull();
    }

    @Test
    @DisplayName("Should map AuthorDTO to Author entity")
    void shouldMapDtoToEntity() {
        // When
        Author result = authorMapper.toEntity(testAuthorDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(testAuthorDTO.name());
        assertThat(result.getBiography()).isEqualTo(testAuthorDTO.biography());
        assertThat(result.getBirthYear()).isEqualTo(testAuthorDTO.birthYear());
        assertThat(result.getDeathYear()).isEqualTo(testAuthorDTO.deathYear());
        // Author always initializes quotes as empty list due to @Builder.Default
        assertThat(result.getQuotes()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should update entity from DTO")
    void shouldUpdateEntityFromDto() {
        // Given
        AuthorDTO updateDTO = new AuthorDTO(
                null,
                "Socrates",
                "Updated biography",
                -470,
                -398,
                null,
                null,
                null
        );

        // When
        authorMapper.updateEntityFromDto(updateDTO, testAuthor);

        // Then
        assertThat(testAuthor.getName()).isEqualTo("Socrates");
        assertThat(testAuthor.getBiography()).isEqualTo("Updated biography");
        assertThat(testAuthor.getBirthYear()).isEqualTo(-470);
        assertThat(testAuthor.getDeathYear()).isEqualTo(-398);
    }

    @Test
    @DisplayName("Should map list of authors to DTOs")
    void shouldMapListToDtos() {
        // Given
        Author author2 = Author.builder()
                .id(2L)
                .name("Plato")
                .build();
        List<Author> authors = List.of(testAuthor, author2);

        // When
        List<AuthorDTO> result = authorMapper.toDtoList(authors);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Socrates");
        assertThat(result.get(1).name()).isEqualTo("Plato");
    }

    @Test
    @DisplayName("Should handle null entity")
    void shouldHandleNullEntity() {
        // When
        AuthorDTO result = authorMapper.toDtoWithoutQuotes(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null DTO")
    void shouldHandleNullDto() {
        // When
        Author result = authorMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should ignore null fields when updating")
    void shouldIgnoreNullFieldsWhenUpdating() {
        // Given
        String originalBiography = testAuthor.getBiography();
        AuthorDTO updateDTO = new AuthorDTO(
                null,
                "Socrates Updated",
                null,  // biography is null
                -469,
                null,  // deathYear is null
                null,
                null,
                null
        );

        // When
        authorMapper.updateEntityFromDto(updateDTO, testAuthor);

        // Then
        assertThat(testAuthor.getName()).isEqualTo("Socrates Updated");
        assertThat(testAuthor.getBiography()).isEqualTo(originalBiography); // Should remain unchanged
        assertThat(testAuthor.getBirthYear()).isEqualTo(-469);
        assertThat(testAuthor.getDeathYear()).isEqualTo(-399); // Should remain unchanged
    }
}
