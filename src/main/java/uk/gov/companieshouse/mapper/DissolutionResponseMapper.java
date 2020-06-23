package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.dto.CreateDissolutionLinksDTO;
import uk.gov.companieshouse.model.dto.CreateDissolutionResponseDTO;

@Service
public class DissolutionResponseMapper {

    public CreateDissolutionResponseDTO mapToCreateDissolutionResponse(Dissolution dissolution) {
        final CreateDissolutionResponseDTO response = new CreateDissolutionResponseDTO();

        response.setApplicationReferenceNumber(dissolution.getData().getApplication().getReference());
        response.setLinks(generateLinks(dissolution.getCompany().getNumber()));

        return response;
    }

    private CreateDissolutionLinksDTO generateLinks(String companyNumber) {
        final CreateDissolutionLinksDTO links = new CreateDissolutionLinksDTO();

        links.setSelf(String.format("/dissolution-request/%s", companyNumber));
        links.setPayment(String.format("/dissolution-request/%s/payment", companyNumber));

        return links;
    }
}
