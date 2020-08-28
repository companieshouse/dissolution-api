package uk.gov.companieshouse.mapper.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.model.db.dissolution.Company;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateCompany;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;
import static uk.gov.companieshouse.fixtures.EmailFixtures.CDN_HOST;
import static uk.gov.companieshouse.fixtures.EmailFixtures.CHS_URL;
import static uk.gov.companieshouse.model.Constants.SIGNATORY_TO_SIGN_EMAIL_SUBJECT;

@ExtendWith(MockitoExtension.class)
public class DissolutionEmailMapperTest {

    private static final String SIGNATORY_TO_SIGN_EMAIL = "signatory@mail.com";
    private static final String SIGNATORY_TO_SIGN_DEADLINE = "17 September 2020";

    @InjectMocks
    private DissolutionEmailMapper dissolutionEmailMapper;

    @Mock
    private EnvironmentConfig environmentConfig;

    @BeforeEach
    public void setup() {
        when(environmentConfig.getChsUrl()).thenReturn(CHS_URL);
        when(environmentConfig.getCdnHost()).thenReturn(CDN_HOST);
    }

    @Test
    public void mapToSuccessfulPaymentEmailData() {
        final Dissolution dissolution = generateDissolution();
        final SuccessfulPaymentEmailData successfulPaymentEmailData = EmailFixtures.generateSuccessfulPaymentEmailData();

        final SuccessfulPaymentEmailData result = dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution);

        assertEquals(successfulPaymentEmailData.getTo(), result.getTo());
        assertEquals(successfulPaymentEmailData.getSubject(), result.getSubject());
        assertEquals(successfulPaymentEmailData.getDissolutionReferenceNumber(), result.getDissolutionReferenceNumber());
        assertEquals(successfulPaymentEmailData.getCompanyNumber(), result.getCompanyNumber());
        assertEquals(successfulPaymentEmailData.getCompanyName(), result.getCompanyName());
        assertEquals(successfulPaymentEmailData.getChsUrl(), result.getChsUrl());
        assertEquals(successfulPaymentEmailData.getCdnHost(), result.getCdnHost());
    }

    @Test
    public void mapToSignatoryToSignEmailData_mapsDissolutionInfo() {
        final Company company = generateCompany();
        company.setName("Some Company Name");
        company.setNumber("12345");

        final Dissolution dissolution = generateDissolution();
        dissolution.setCompany(company);
        dissolution.getData().getApplication().setReference("abc123");

        final SignatoryToSignEmailData result = dissolutionEmailMapper.mapToSignatoryToSignEmailData(dissolution, SIGNATORY_TO_SIGN_EMAIL, SIGNATORY_TO_SIGN_DEADLINE);

        assertEquals("Some Company Name", result.getCompanyName());
        assertEquals("12345", result.getCompanyNumber());
        assertEquals("abc123", result.getDissolutionReferenceNumber());
    }

    @Test
    public void mapToSignatoryToSignEmailData_mapsSignatoryInfo_andDeadline_andSubject() {
        final SignatoryToSignEmailData result = dissolutionEmailMapper.mapToSignatoryToSignEmailData(generateDissolution(), SIGNATORY_TO_SIGN_EMAIL, SIGNATORY_TO_SIGN_DEADLINE);

        assertEquals(SIGNATORY_TO_SIGN_EMAIL, result.getTo());
        assertEquals(SIGNATORY_TO_SIGN_DEADLINE, result.getDissolutionDeadlineDate());
        assertEquals(SIGNATORY_TO_SIGN_EMAIL_SUBJECT, result.getSubject());
    }

    @Test
    public void mapToSignatoryToSignEmailData_mapsEnvironmentInfo() {
        final SignatoryToSignEmailData result = dissolutionEmailMapper.mapToSignatoryToSignEmailData(generateDissolution(), SIGNATORY_TO_SIGN_EMAIL, SIGNATORY_TO_SIGN_DEADLINE);

        assertEquals(CHS_URL, result.getChsUrl());
        assertEquals(CDN_HOST, result.getCdnHost());
    }
}
