package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.dto.CreateDissolutionLinksDTO;
import uk.gov.companieshouse.model.dto.CreateDissolutionResponseDTO;

@Service
public class DissolutionResponseMapper {

    public CreateDissolutionResponseDTO mapToCreateDissolutionResponse(Dissolution dissolution) {
        return new CreateDissolutionResponseDTO() {{
            setApplicationReferenceNumber(dissolution.getData().getApplication().getReference());
            setLinks(generateLinks(dissolution.getCompany().getNumber()));
        }};
    }

    private CreateDissolutionLinksDTO generateLinks(String companyNumber) {
        return new CreateDissolutionLinksDTO() {{
            setSelf(String.format("/dissolution-request/%s", companyNumber));
            setPayment(String.format("/dissolution-request/%s/payment", companyNumber));
        }};
    }
}
