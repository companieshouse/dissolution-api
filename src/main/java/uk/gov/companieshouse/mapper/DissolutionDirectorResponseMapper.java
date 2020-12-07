package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionLinks;

@Service
public class DissolutionDirectorResponseMapper {

    public DissolutionDirectorPatchResponse mapToDissolutionDirectorPatchResponse(Dissolution dissolution) {
        final DissolutionDirectorPatchResponse response = new DissolutionDirectorPatchResponse();

        response.setLinks(generateLinks(
                dissolution.getCompany().getNumber(),
                dissolution.getData().getApplication().getReference()
        ));

        return response;
    }

    private DissolutionLinks generateLinks(String companyNumber, String reference) {
        final DissolutionLinks links = new DissolutionLinks();

        links.setSelf(String.format("/dissolution-request/%s", companyNumber));
        links.setPayment(String.format("/dissolution-request/%s/payment", reference));

        return links;
    }
}
