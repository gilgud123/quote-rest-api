package com.katya.quoterestapi.service;

import com.katya.quoterestapi.dto.AuthorDTO;
import com.katya.quoterestapi.entity.Author;
import com.katya.quoterestapi.exception.ResourceAlreadyExistsException;
import com.katya.quoterestapi.exception.ResourceNotFoundException;
import com.katya.quoterestapi.mapper.AuthorMapper;
import com.katya.quoterestapi.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service class for Author-related business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    /**
     * Get all authors with pagination
     */
    public Page<AuthorDTO> getAllAuthors(Pageable pageable) {
        log.debug("Fetching all authors with pagination: {}", pageable);
        return authorRepository.findAll(pageable)
                .map(authorMapper::toDtoWithoutQuotes);
    }

    /**
     * Get author by ID (with quotes)
     */
    public AuthorDTO getAuthorById(Long id) {
        log.debug("Fetching author with id: {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
        return authorMapper.toDto(author);
    }

    /**
     * Get author by ID (without quotes)
     */
    public AuthorDTO getAuthorByIdWithoutQuotes(Long id) {
        log.debug("Fetching author (without quotes) with id: {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
        return authorMapper.toDtoWithoutQuotes(author);
    }

    /**
     * Search authors by name
     */
    public Page<AuthorDTO> searchAuthorsByName(String name, Pageable pageable) {
        log.debug("Searching authors by name: {}", name);
        return authorRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(authorMapper::toDtoWithoutQuotes);
    }

    /**
     * Filter authors by birth year
     */
    public Page<AuthorDTO> filterAuthorsByBirthYear(Integer birthYear, Pageable pageable) {
        log.debug("Filtering authors by birth year: {}", birthYear);
        return authorRepository.findByBirthYear(birthYear, pageable)
                .map(authorMapper::toDtoWithoutQuotes);
    }

    /**
     * Filter authors by birth year range
     */
    public Page<AuthorDTO> filterAuthorsByBirthYearRange(Integer minYear, Integer maxYear, Pageable pageable) {
        log.debug("Filtering authors by birth year range: {} - {}", minYear, maxYear);
        return authorRepository.findByBirthYearBetween(minYear, maxYear, pageable)
                .map(authorMapper::toDtoWithoutQuotes);
    }

    /**
     * Create a new author
     */
    @Transactional
    public AuthorDTO createAuthor(AuthorDTO authorDTO) {
        log.debug("Creating new author: {}", authorDTO.name());

        // Check if author with same name already exists
        if (authorRepository.existsByName(authorDTO.name())) {
            throw new ResourceAlreadyExistsException("Author", "name", authorDTO.name());
        }

        Author author = authorMapper.toEntity(authorDTO);
        Author savedAuthor = authorRepository.save(author);
        log.info("Created author with id: {}", savedAuthor.getId());

        return authorMapper.toDtoWithoutQuotes(savedAuthor);
    }

    /**
     * Update an existing author
     */
    @Transactional
    public AuthorDTO updateAuthor(Long id, AuthorDTO authorDTO) {
        log.debug("Updating author with id: {}", id);

        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));

        // Check if new name conflicts with another author
        if (authorDTO.name() != null && !authorDTO.name().equals(existingAuthor.getName())) {
            if (authorRepository.existsByName(authorDTO.name())) {
                throw new ResourceAlreadyExistsException("Author", "name", authorDTO.name());
            }
        }

        authorMapper.updateEntityFromDto(authorDTO, existingAuthor);
        Author updatedAuthor = authorRepository.save(existingAuthor);
        log.info("Updated author with id: {}", id);

        return authorMapper.toDtoWithoutQuotes(updatedAuthor);
    }

    /**
     * Partially update an existing author
     */
    @Transactional
    public AuthorDTO patchAuthor(Long id, AuthorDTO authorDTO) {
        log.debug("Partially updating author with id: {}", id);
        return updateAuthor(id, authorDTO); // Using the same logic for patch
    }

    /**
     * Delete an author by ID
     */
    @Transactional
    public void deleteAuthor(Long id) {
        log.debug("Deleting author with id: {}", id);

        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author", id);
        }

        authorRepository.deleteById(id);
        log.info("Deleted author with id: {}", id);
    }

    /**
     * Get count of quotes for an author
     */
    public long getQuoteCountForAuthor(Long authorId) {
        log.debug("Getting quote count for author with id: {}", authorId);

        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author", authorId);
        }

        return authorRepository.countQuotesByAuthorId(authorId);
    }

    /**
     * Check if author exists
     */
    public boolean authorExists(Long id) {
        return authorRepository.existsById(id);
    }
}
