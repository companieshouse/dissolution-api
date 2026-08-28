package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.model.domain.DissolutionInitiationCommand;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionInitiationRequest;

@Component
public class DissolutionInitiationMapper {

    public DissolutionInitiationCommand toCommand(Transaction transaction, String companyNumber, String userId, DissolutionInitiationRequest request) {
        return new DissolutionInitiationCommand(transaction, companyNumber, userId, request.getDirectors());
    }
}
