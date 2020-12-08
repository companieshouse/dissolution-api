package uk.gov.companieshouse.mapper;

import uk.gov.companieshouse.model.dto.dissolution.DissolutionLinks;

public class ResponseMapper {
    protected DissolutionLinks generateLinks(String companyNumber, String reference) {
        final DissolutionLinks links = new DissolutionLinks();

        links.setSelf(String.format("/dissolution-request/%s", companyNumber));
        links.setPayment(String.format("/dissolution-request/%s/payment", reference));

        return links;
    }
}
