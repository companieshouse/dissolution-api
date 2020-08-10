package uk.gov.companieshouse.mapper.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.EmailFixtures.CDN_HOST;
import static uk.gov.companieshouse.fixtures.EmailFixtures.CDN_URL;

@ExtendWith(MockitoExtension.class)
public class DissolutionEmailMapperTest {

    @InjectMocks
    private DissolutionEmailMapper dissolutionEmailMapper;

    @Mock
    private EnvironmentConfig environmentConfig;

    @Test
    public void mapToSuccessfulPaymentEmailData() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final SuccessfulPaymentEmailData successfulPaymentEmailData = EmailFixtures.generateSuccessfulPaymentEmailData();

        when(environmentConfig.getChsUrl()).thenReturn(CDN_URL);
        when(environmentConfig.getCdnHost()).thenReturn(CDN_HOST);

        final SuccessfulPaymentEmailData result = dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution);

        assertEquals(successfulPaymentEmailData.getTo(), result.getTo());
        assertEquals(successfulPaymentEmailData.getSubject(), result.getSubject());
        assertEquals(successfulPaymentEmailData.getDissolutionReferenceNumber(), result.getDissolutionReferenceNumber());
        assertEquals(successfulPaymentEmailData.getCompanyNumber(), result.getCompanyNumber());
        assertEquals(successfulPaymentEmailData.getCompanyName(), result.getCompanyName());
        assertEquals(successfulPaymentEmailData.getChsUrl(), result.getChsUrl());
        assertEquals(successfulPaymentEmailData.getCdnHost(), result.getCdnHost());
    }
}
