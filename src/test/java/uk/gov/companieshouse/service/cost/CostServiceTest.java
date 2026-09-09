package uk.gov.companieshouse.service.cost;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.model.domain.DissolutionCost;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@ExtendWith(MockitoExtension.class)
class CostServiceTest {

    private static final String DISSOLUTION_ID = "12345678";
    private static final String TRANSACTION_ID = "tx-id-123";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NAME = "Test Company";

    @Mock
    private DissolutionService dissolutionService;

    @Mock
    private FeeConfig feeConfig;

    @InjectMocks
    private CostService costService;

    private Transaction transaction;

    @BeforeEach
    void setup() {
        transaction = TransactionTestDataBuilder.aTransaction()
                .withId(TRANSACTION_ID)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID))
                .build();
    }

    @Test
    void when_valid_dissolution_then_dissolutionCost_returned() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        final var dissolution = aDissolution()
                .withId(DISSOLUTION_ID)
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(COMPANY_NAME)
                .build();

        when(dissolutionService.getDissolutionById(DISSOLUTION_ID)).thenReturn(dissolution);
        when(feeConfig.getClosingPounds()).thenReturn("10.00");

        var actualCost = costService.getCosts(transaction, DISSOLUTION_ID);

        assertThat(actualCost).isEqualTo(new DissolutionCost("10.00", COMPANY_NAME, COMPANY_NUMBER, ApplicationType.DS01.getValue()));
    }

    @Test
    void when_dissolution_not_found_then_exception_thrown() {
        when(dissolutionService.getDissolutionById(DISSOLUTION_ID))
                .thenThrow(new DissolutionNotFoundException());

        assertThatThrownBy(() -> costService.getCosts(transaction, DISSOLUTION_ID))
                .isInstanceOf(DissolutionNotFoundException.class);
    }

    @Test
    void when_dissolution_not_linked_to_transaction_then_exception_thrown() {
        transaction = TransactionTestDataBuilder.aTransaction().withId(TRANSACTION_ID).build();

        assertThatThrownBy(() -> costService.getCosts(transaction, DISSOLUTION_ID))
                .isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }
}
