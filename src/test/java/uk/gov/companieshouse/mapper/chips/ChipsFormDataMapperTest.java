package uk.gov.companieshouse.mapper.chips;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsFormData;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsFormType;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsPaymentMethod;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentInformation;

@ExtendWith(MockitoExtension.class)
public class ChipsFormDataMapperTest {

    private static final String APPLICANT_EMAIL = "applicant@mail.com";
    private static final String COMPANY_NAME = "Some Company Name";
    private static final String COMPANY_NUMBER = "12345";
    private static final String DISSOLUTION_REFERENCE = "someRef";
    private static final String DISSOLUTION_BARCODE = "B4RC0D3";
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String ACCOUNT_NUMBER = "222222";

    @InjectMocks
    private ChipsFormDataMapper mapper;

    @Mock
    private XmlMapper xmlMapper;

    private Dissolution dissolution;
    private ArgumentCaptor<ChipsFormData> requestCaptor;

    @BeforeEach
    public void setup() throws Exception {
        dissolution = generateDissolution();
        dissolution.setPaymentInformation(generatePaymentInformation());
        dissolution.getData().setDirectors(Collections.emptyList());

        requestCaptor = ArgumentCaptor.forClass(ChipsFormData.class);

        when(xmlMapper.writeValueAsString(any())).thenReturn("some xml");
    }

    @Test
    public void mapToChipsFormDataXml_writesToAnXmlString_andReturnsIt() throws Exception {
        final String result = mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(any());

        assertEquals("some xml", result);
    }

    @Test
    public void mapToChipsFormDataXml_setsTheFormTypeForDs01() throws Exception {
        dissolution.getData().getApplication().setType(ApplicationType.DS01);

        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(ChipsFormType.DS01, request.getType());
    }

    @Test
    public void mapToChipsFormDataXml_setsTheFormTypeForLlds01() throws Exception {
        dissolution.getData().getApplication().setType(ApplicationType.LLDS01);

        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(ChipsFormType.LLDS01, request.getType());
    }

    @Test
    public void mapToChipsFormDataXml_setsTheFormVersion() throws Exception {
        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(1, request.getVersion());
    }

    @Test
    public void mapToChipsFormDataXml_setsTheFilingDetailsCorrectly() throws Exception {
        dissolution.getData().getApplication().setReference(DISSOLUTION_REFERENCE);
        dissolution.getData().getApplication().setBarcode(DISSOLUTION_BARCODE);
        dissolution.getCreatedBy().setDateTime(LocalDateTime.of(2020, 1, 1, 0, 0));
        dissolution.getPaymentInformation().setDateTime(LocalDateTime.of(2020, 2, 2, 0, 0));

        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(DISSOLUTION_BARCODE, request.getFilingDetails().getBarcode());
        assertEquals(DISSOLUTION_REFERENCE, request.getFilingDetails().getPresenterDocumentReference());
        assertEquals(DISSOLUTION_REFERENCE, request.getFilingDetails().getPackageIdentifier());
        assertEquals(DISSOLUTION_REFERENCE, request.getFilingDetails().getSubmissionReference());
        assertEquals("enablement", request.getFilingDetails().getMethod());
        assertEquals(1, request.getFilingDetails().getPackageCount());
        assertEquals("2020-01-01", request.getFilingDetails().getSignDate());
        assertEquals("2020-02-02", request.getFilingDetails().getReceiptDate());
    }

    @Test
    public void mapToChipsFormDataXml_setsTheFilingDetails_presenterDetailsCorrectly() throws Exception {
        dissolution.getCreatedBy().setEmail(APPLICANT_EMAIL);

        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(APPLICANT_EMAIL, request.getFilingDetails().getPresenterDetails().getPresenterEmailIn());
        assertEquals(APPLICANT_EMAIL, request.getFilingDetails().getPresenterDetails().getPresenterEmailOut());
    }

    @Test
    public void mapToChipsFormDataXml_setsTheFilingDetails_paymentDetailsCorrectly_payByAccountFeatureToggleOn() throws Exception {
        dissolution.getPaymentInformation().setMethod(PaymentMethod.ACCOUNT);
        dissolution.getPaymentInformation().setAccountNumber(ACCOUNT_NUMBER);

        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(ChipsPaymentMethod.ACCOUNT, request.getFilingDetails().getPayment().getPaymentMethod());
        assertEquals(ACCOUNT_NUMBER, request.getFilingDetails().getPayment().getAccountNumber());
        Assert.assertNull(request.getFilingDetails().getPayment().getReferenceNumber());
    }

    @Test
    public void mapToChipsFormDataXml_setsTheFilingDetails_paymentDetailsCorrectly_payByAccountFeatureToggleOff() throws Exception {
        dissolution.getPaymentInformation().setMethod(PaymentMethod.CREDIT_CARD);
        dissolution.getPaymentInformation().setReference(PAYMENT_REFERENCE);

        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(ChipsPaymentMethod.CREDIT_CARD, request.getFilingDetails().getPayment().getPaymentMethod());
        assertEquals(PAYMENT_REFERENCE, request.getFilingDetails().getPayment().getReferenceNumber());
        Assert.assertNull(request.getFilingDetails().getPayment().getAccountNumber());
    }

    @Test
    public void mapToChipsFormDataXml_setsTheCorporateBody_companyInfoCorrectly() throws Exception {
        dissolution.getCompany().setName(COMPANY_NAME);
        dissolution.getCompany().setNumber(COMPANY_NUMBER);

        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(COMPANY_NAME, request.getCorporateBody().getCorporateBodyName());
        assertEquals(COMPANY_NUMBER, request.getCorporateBody().getIncorporationNumber());
    }

    @Test
    public void mapToChipsFormDataXml_setsTheCorporateBody_officersCorrectly() throws Exception {
        DirectorApproval approvalOne = generateDirectorApproval();
        approvalOne.setDateTime(LocalDateTime.of(2020, 1, 1, 12, 30));
        approvalOne.setIpAddress("192.168.0.2");

        DissolutionDirector directorOne = generateDissolutionDirector();
        directorOne.setName("DOE, John James");
        directorOne.setDirectorApproval(approvalOne);
        directorOne.setEmail("mail1");

        DirectorApproval approvalTwo = generateDirectorApproval();
        approvalTwo.setIpAddress("192.168.0.3");

        approvalTwo.setDateTime(LocalDateTime.of(2020, 2, 2, 16, 15));

        DissolutionDirector directorTwo = generateDissolutionDirector();
        directorTwo.setName("ST.MARCUS CORPORATION");
        directorTwo.setDirectorApproval(approvalTwo);
        directorTwo.setEmail("mail2");
        directorTwo.setOnBehalfName("Mr. Accountant");

        dissolution.getData().setDirectors(Arrays.asList(directorOne, directorTwo));

        mapper.mapToChipsFormDataXml(dissolution);

        verify(xmlMapper).writeValueAsString(requestCaptor.capture());

        final ChipsFormData request = requestCaptor.getValue();

        assertEquals(2, request.getCorporateBody().getOfficers().size());

        assertEquals("John James", request.getCorporateBody().getOfficers().get(0).getPersonName().getForename());
        assertEquals("DOE", request.getCorporateBody().getOfficers().get(0).getPersonName().getSurname());
        assertEquals("2020-01-01", request.getCorporateBody().getOfficers().get(0).getSignDate());
        assertEquals("mail1", request.getCorporateBody().getOfficers().get(0).getEmail());
        assertEquals("192.168.0.2", request.getCorporateBody().getOfficers().get(0).getIpAddress());

        assertNull(request.getCorporateBody().getOfficers().get(1).getPersonName().getForename());
        assertEquals("ST.MARCUS CORPORATION", request.getCorporateBody().getOfficers().get(1).getPersonName().getSurname());
        assertEquals("2020-02-02", request.getCorporateBody().getOfficers().get(1).getSignDate());
        assertEquals("mail2", request.getCorporateBody().getOfficers().get(1).getEmail());
        assertEquals("Mr. Accountant", request.getCorporateBody().getOfficers().get(1).getOnBehalfName());
        assertEquals("192.168.0.3", request.getCorporateBody().getOfficers().get(1).getIpAddress());
    }
}
