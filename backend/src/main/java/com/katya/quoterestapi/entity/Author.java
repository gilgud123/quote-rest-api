package com.katya.quoterestapi.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;

/**
 * Entity representing an Author in the system. One author can have multiple quotes (one-to-many
 * relationship).
 */
@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(100)")
  private String name;

  @Column(columnDefinition = "VARCHAR(1000)")
  private String biography;

  @Column(name = "birth_year")
  private Integer birthYear;

  @Column(name = "death_year")
  private Integer deathYear;

  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Quote> quotes = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /** Helper method to add a quote to the author */
  public void addQuote(Quote quote) {
    quotes.add(quote);
    quote.setAuthor(this);
  }

  /** Helper method to remove a quote from the author */
  public void removeQuote(Quote quote) {
    quotes.remove(quote);
    quote.setAuthor(null);
  }
}
