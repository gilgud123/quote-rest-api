package com.katya.quoterestapi.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.*;

import com.katya.quoterestapi.dto.AuthorDTO;
import com.katya.quoterestapi.entity.Author;

/** MapStruct mapper for converting between Author entity and AuthorDTO. */
@Mapper(
    componentModel = "spring",
    uses = {QuoteMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuthorMapper {

  /** Convert Author entity to AuthorDTO (including quotes) */
  @Named("toDto")
  @Mapping(target = "quotes", source = "quotes")
  AuthorDTO toDto(Author author);

  /** Convert Author entity to AuthorDTO (without quotes) */
  @Named("toDtoWithoutQuotes")
  @Mapping(target = "quotes", ignore = true)
  AuthorDTO toDtoWithoutQuotes(Author author);

  /** Convert AuthorDTO to Author entity */
  @Mapping(target = "quotes", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Author toEntity(AuthorDTO authorDTO);

  /** Update existing Author entity from AuthorDTO */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "quotes", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(AuthorDTO authorDTO, @MappingTarget Author author);

  /** Convert list of Author entities to list of AuthorDTOs (without quotes) */
  default List<AuthorDTO> toDtoList(List<Author> authors) {
    return authors.stream().map(this::toDtoWithoutQuotes).collect(Collectors.toList());
  }
}
