package uk.gov.companieshouse.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalCommand;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;

@Mapper(componentModel = "spring")
public interface DissolutionDirectorApprovalMapper {
    DissolutionDirectorApprovalCommand toCommand(String userId, DissolutionPatchRequest request);
}
