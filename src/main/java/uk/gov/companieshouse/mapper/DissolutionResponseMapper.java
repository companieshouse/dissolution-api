package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.dto.DissolutionCreateLinks;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;

@Service
public class DissolutionResponseMapper {

    public DissolutionCreateResponse mapToDissolutionCreateResponse(Dissolution dissolution) {
        final DissolutionCreateResponse response = new DissolutionCreateResponse();

        response.setApplicationReferenceNumber(dissolution.getData().getApplication().getReference());
        response.setLinks(generateLinks(dissolution.getCompany().getNumber()));

        return response;
    }

    private DissolutionCreateLinks generateLinks(String companyNumber) {
        final DissolutionCreateLinks links = new DissolutionCreateLinks();

        links.setSelf(String.format("/dissolution-request/%s", companyNumber));
        links.setPayment(String.format("/dissolution-request/%s/payment", companyNumber));

        return links;
    }
}
