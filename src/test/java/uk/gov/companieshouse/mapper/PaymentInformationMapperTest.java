package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class PaymentInformationMapperTest {
    private static final PaymentMethod METHOD = PaymentMethod.CREDIT_CARD;
    private static final String REFERENCE = "TEST_REFERENCE";
    private static final LocalDateTime DATE = LocalDateTime.now();
    private static final Timestamp TIMESTAMP = Timestamp.valueOf(DATE);

    private final PaymentInformationMapper mapper = new PaymentInformationMapper();

    @Test
    public void mapToPaymentInformation_getsPaymentInformation() {
        final PaymentInformation paymentInformation = mapper.mapToPaymentInformation(PaymentMethod.CREDIT_CARD, REFERENCE, TIMESTAMP);

        assertEquals(DATE, paymentInformation.getDateTime());
        assertEquals(REFERENCE, paymentInformation.getReference());
        assertEquals(METHOD, paymentInformation.getMethod());
    }
}
