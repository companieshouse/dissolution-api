package uk.gov.companieshouse.model.dto.dissolution;

public record DissolutionLinks(String self, String payment) {

    public static DissolutionLinks of(String companyNumber, String reference) {
        return new DissolutionLinks(
                String.format("/dissolution-request/%s", companyNumber),
                String.format("/dissolution-request/%s/payment", reference)
        );
    }

    public static DissolutionLinks forTransaction(String companyNumber, String transactionId) {
        return new DissolutionLinks(
                String.format("/company/%s/transaction/%s/dissolution", companyNumber, transactionId),
                null
        );
    }
}
