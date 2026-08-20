package uk.gov.companieshouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.model.db.dissolution.CreatedBy;
import uk.gov.companieshouse.model.domain.DissolutionUserData;
import uk.gov.companieshouse.util.DateTimeGenerator;

@Mapper(componentModel = "spring", imports = DateTimeGenerator.class)
public interface CreatedByMapper {

    @Mapping(target = "dateTime", expression = "java(DateTimeGenerator.generateCurrentDateTime())")
    CreatedBy mapToCreatedBy(DissolutionUserData userData);
}