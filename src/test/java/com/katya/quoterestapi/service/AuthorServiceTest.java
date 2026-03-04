package com.katya.quoterestapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.katya.quoterestapi.dto.AuthorDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.exception.ResourceAlreadyExistsException;
import com.katya.quoterestapi.exception.ResourceNotFoundException;
import com.katya.quoterestapi.mapper.AuthorMapper;
import com.katya.quoterestapi.repository.AuthorRepository;

/** Unit tests for AuthorService */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorService Unit Tests")
class AuthorServiceTest {

  @Mock private AuthorRepository authorRepository;

  @Mock private AuthorMapper authorMapper;

  @InjectMocks private AuthorService authorService;

  private Author testAuthor;
  private AuthorDTO testAuthorDTO;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testAuthor =
        Author.builder()
            .id(1L)
            .name("Socrates")
            .biography("Ancient Greek philosopher")
            .birthYear(-469)
            .deathYear(-399)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    testAuthorDTO =
        new AuthorDTO(
            1L,
            "Socrates",
            "Ancient Greek philosopher",
            -469,
            -399,
            null,
            LocalDateTime.now(),
            LocalDateTime.now());

    pageable = PageRequest.of(0, 10);
  }

  @Test
  @DisplayName("Should get all authors with pagination")
  void shouldGetAllAuthors() {
    // Given
    Page<Author> authorPage = new PageImpl<>(List.of(testAuthor));
    when(authorRepository.findAll(pageable)).thenReturn(authorPage);
    when(authorMapper.toDtoWithoutQuotes(testAuthor)).thenReturn(testAuthorDTO);

    // When
    Page<AuthorDTO> result = authorService.getAllAuthors(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("Socrates");
    verify(authorRepository).findAll(pageable);
    verify(authorMapper).toDtoWithoutQuotes(testAuthor);
  }

  @Test
  @DisplayName("Should get author by ID")
  void shouldGetAuthorById() {
    // Given
    when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
    when(authorMapper.toDto(testAuthor)).thenReturn(testAuthorDTO);

    // When
    AuthorDTO result = authorService.getAuthorById(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("Socrates");
    verify(authorRepository).findById(1L);
    verify(authorMapper).toDto(testAuthor);
  }

  @Test
  @DisplayName("Should throw exception when author not found by ID")
  void shouldThrowExceptionWhenAuthorNotFound() {
    // Given
    when(authorRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authorService.getAuthorById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author not found with id: 999");
    verify(authorRepository).findById(999L);
    verify(authorMapper, never()).toDto(any());
  }

  @Test
  @DisplayName("Should search authors by name")
  void shouldSearchAuthorsByName() {
    // Given
    String searchName = "Socrates";
    Page<Author> authorPage = new PageImpl<>(List.of(testAuthor));
    when(authorRepository.findByNameContainingIgnoreCase(searchName, pageable))
        .thenReturn(authorPage);
    when(authorMapper.toDtoWithoutQuotes(testAuthor)).thenReturn(testAuthorDTO);

    // When
    Page<AuthorDTO> result = authorService.searchAuthorsByName(searchName, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).contains("Socrates");
    verify(authorRepository).findByNameContainingIgnoreCase(searchName, pageable);
  }

  @Test
  @DisplayName("Should create new author")
  void shouldCreateAuthor() {
    // Given
    AuthorDTO createDTO = new AuthorDTO("Plato", "Student of Socrates", -428, -348);
    Author newAuthor = Author.builder().name("Plato").build();
    Author savedAuthor = Author.builder().id(2L).name("Plato").build();

    when(authorRepository.existsByName("Plato")).thenReturn(false);
    when(authorMapper.toEntity(createDTO)).thenReturn(newAuthor);
    when(authorRepository.save(newAuthor)).thenReturn(savedAuthor);
    when(authorMapper.toDtoWithoutQuotes(savedAuthor))
        .thenReturn(new AuthorDTO(2L, "Plato", "Student of Socrates", -428, -348, null, null));

    // When
    AuthorDTO result = authorService.createAuthor(createDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(2L);
    assertThat(result.name()).isEqualTo("Plato");
    verify(authorRepository).existsByName("Plato");
    verify(authorRepository).save(newAuthor);
  }

  @Test
  @DisplayName("Should throw exception when creating author with duplicate name")
  void shouldThrowExceptionWhenCreatingDuplicateAuthor() {
    // Given
    AuthorDTO createDTO = new AuthorDTO("Socrates", "Biography", -469, -399);
    when(authorRepository.existsByName("Socrates")).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> authorService.createAuthor(createDTO))
        .isInstanceOf(ResourceAlreadyExistsException.class)
        .hasMessageContaining("Author already exists with name: Socrates");
    verify(authorRepository).existsByName("Socrates");
    verify(authorRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update existing author")
  void shouldUpdateAuthor() {
    // Given
    AuthorDTO updateDTO =
        new AuthorDTO(null, "Socrates", "Updated biography", -469, -399, null, null);
    when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
    when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
    when(authorMapper.toDtoWithoutQuotes(testAuthor)).thenReturn(testAuthorDTO);

    // When
    AuthorDTO result = authorService.updateAuthor(1L, updateDTO);

    // Then
    assertThat(result).isNotNull();
    verify(authorRepository).findById(1L);
    verify(authorMapper).updateEntityFromDto(updateDTO, testAuthor);
    verify(authorRepository).save(testAuthor);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent author")
  void shouldThrowExceptionWhenUpdatingNonExistentAuthor() {
    // Given
    AuthorDTO updateDTO = new AuthorDTO("Updated", "Bio", -400, -350);
    when(authorRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authorService.updateAuthor(999L, updateDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author not found with id: 999");
    verify(authorRepository).findById(999L);
    verify(authorRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete author")
  void shouldDeleteAuthor() {
    // Given
    when(authorRepository.existsById(1L)).thenReturn(true);
    doNothing().when(authorRepository).deleteById(1L);

    // When
    authorService.deleteAuthor(1L);

    // Then
    verify(authorRepository).existsById(1L);
    verify(authorRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent author")
  void shouldThrowExceptionWhenDeletingNonExistentAuthor() {
    // Given
    when(authorRepository.existsById(999L)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> authorService.deleteAuthor(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author not found with id: 999");
    verify(authorRepository).existsById(999L);
    verify(authorRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should filter authors by birth year")
  void shouldFilterAuthorsByBirthYear() {
    // Given
    Integer birthYear = -469;
    Page<Author> authorPage = new PageImpl<>(List.of(testAuthor));
    when(authorRepository.findByBirthYear(birthYear, pageable)).thenReturn(authorPage);
    when(authorMapper.toDtoWithoutQuotes(testAuthor)).thenReturn(testAuthorDTO);

    // When
    Page<AuthorDTO> result = authorService.filterAuthorsByBirthYear(birthYear, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(authorRepository).findByBirthYear(birthYear, pageable);
  }

  @Test
  @DisplayName("Should get quote count for author")
  void shouldGetQuoteCountForAuthor() {
    // Given
    when(authorRepository.existsById(1L)).thenReturn(true);
    when(authorRepository.countQuotesByAuthorId(1L)).thenReturn(5L);

    // When
    long count = authorService.getQuoteCountForAuthor(1L);

    // Then
    assertThat(count).isEqualTo(5L);
    verify(authorRepository).existsById(1L);
    verify(authorRepository).countQuotesByAuthorId(1L);
  }

  @Test
  @DisplayName("Should check if author exists")
  void shouldCheckIfAuthorExists() {
    // Given
    when(authorRepository.existsById(1L)).thenReturn(true);
    when(authorRepository.existsById(999L)).thenReturn(false);

    // When
    boolean exists = authorService.authorExists(1L);
    boolean notExists = authorService.authorExists(999L);

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
    verify(authorRepository, times(2)).existsById(any());
  }

  @Test
  @DisplayName("Should get author by ID without quotes")
  void shouldGetAuthorByIdWithoutQuotes() {
    when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
    when(authorMapper.toDtoWithoutQuotes(testAuthor)).thenReturn(testAuthorDTO);

    AuthorDTO result = authorService.getAuthorByIdWithoutQuotes(1L);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    verify(authorRepository).findById(1L);
    verify(authorMapper).toDtoWithoutQuotes(testAuthor);
  }

  @Test
  @DisplayName("Should filter authors by birth year range")
  void shouldFilterAuthorsByBirthYearRange() {
    Page<Author> authorPage = new PageImpl<>(List.of(testAuthor));
    when(authorRepository.findByBirthYearBetween(-500, -300, pageable)).thenReturn(authorPage);
    when(authorMapper.toDtoWithoutQuotes(testAuthor)).thenReturn(testAuthorDTO);

    Page<AuthorDTO> result = authorService.filterAuthorsByBirthYearRange(-500, -300, pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(authorRepository).findByBirthYearBetween(-500, -300, pageable);
  }

  @Test
  @DisplayName("Should throw exception when updating author with duplicate name")
  void shouldThrowExceptionWhenUpdatingWithDuplicateName() {
    AuthorDTO updateDTO = new AuthorDTO(null, "Plato", "Bio", -428, -348, null, null);
    when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
    when(authorRepository.existsByName("Plato")).thenReturn(true);

    assertThatThrownBy(() -> authorService.updateAuthor(1L, updateDTO))
        .isInstanceOf(ResourceAlreadyExistsException.class)
        .hasMessageContaining("Author already exists with name: Plato");
    verify(authorRepository).findById(1L);
    verify(authorRepository).existsByName("Plato");
    verify(authorRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should patch author using update logic")
  void shouldPatchAuthor() {
    AuthorDTO patchDTO =
        new AuthorDTO(null, "Socrates", "Patched biography", -469, -399, null, null);
    when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
    when(authorRepository.save(testAuthor)).thenReturn(testAuthor);
    when(authorMapper.toDtoWithoutQuotes(testAuthor)).thenReturn(testAuthorDTO);

    AuthorDTO result = authorService.patchAuthor(1L, patchDTO);

    assertThat(result).isNotNull();
    verify(authorRepository).findById(1L);
    verify(authorMapper).updateEntityFromDto(patchDTO, testAuthor);
    verify(authorRepository).save(testAuthor);
  }

  @Test
  @DisplayName("Should throw exception when getting quote count for missing author")
  void shouldThrowExceptionWhenGetQuoteCountMissingAuthor() {
    when(authorRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> authorService.getQuoteCountForAuthor(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Author not found with id: 999");
    verify(authorRepository).existsById(999L);
    verify(authorRepository, never()).countQuotesByAuthorId(any());
  }
}
