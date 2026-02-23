package com.katya.quoterestapi.mapper;

import com.katya.quoterestapi.dto.QuoteDTO;
import com.katya.quoterestapi.entity.Quote;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between Quote entity and QuoteDTO.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface QuoteMapper {

    /**
     * Convert Quote entity to QuoteDTO
     */
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.name")
    QuoteDTO toDto(Quote quote);

    /**
     * Convert QuoteDTO to Quote entity
     * Note: Author relationship must be set separately in the service layer
     */
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Quote toEntity(QuoteDTO quoteDTO);

    /**
     * Update existing Quote entity from QuoteDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(QuoteDTO quoteDTO, @MappingTarget Quote quote);

    /**
     * Convert list of Quote entities to list of QuoteDTOs
     */
    List<QuoteDTO> toDtoList(List<Quote> quotes);
}
