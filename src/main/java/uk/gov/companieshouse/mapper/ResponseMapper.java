package uk.gov.companieshouse.mapper;

import uk.gov.companieshouse.model.dto.dissolution.DissolutionLinks;

import static uk.gov.companieshouse.model.Constants.DISSOLUTION_BASE_URI_PATTERN;

public abstract class ResponseMapper {
    protected DissolutionLinks generateLinks(String companyNumber, String reference) {
        final DissolutionLinks links = new DissolutionLinks();

        links.setSelf(String.format("/dissolution-request/%s", companyNumber));
        links.setPayment(String.format("/dissolution-request/%s/payment", reference));

        return links;
    }

    protected DissolutionLinks generateDissolutionLinks(String companyNumber, String transactionId) {
        final DissolutionLinks links = new DissolutionLinks();
        links.setSelf(String.format(DISSOLUTION_BASE_URI_PATTERN, companyNumber, transactionId));
        return links;
    }
}
