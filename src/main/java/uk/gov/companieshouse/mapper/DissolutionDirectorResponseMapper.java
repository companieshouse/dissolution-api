package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;

@Service
public class DissolutionDirectorResponseMapper extends ResponseMapper {

    public DissolutionDirectorPatchResponse mapToDissolutionDirectorPatchResponse(Dissolution dissolution) {
        final DissolutionDirectorPatchResponse response = new DissolutionDirectorPatchResponse();

        response.setLinks(generateLinks(
                dissolution.getCompany().getNumber(),
                dissolution.getData().getApplication().getReference()
        ));

        return response;
    }
}
