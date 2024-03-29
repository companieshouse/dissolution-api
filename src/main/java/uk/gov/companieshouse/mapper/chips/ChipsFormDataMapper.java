package uk.gov.companieshouse.mapper.chips;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.ChipsMapperException;
import uk.gov.companieshouse.model.db.dissolution.Company;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsCorporateBody;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsFilingDetails;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsFormData;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsFormType;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsOfficer;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsPayment;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsPaymentMethod;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsPersonName;
import uk.gov.companieshouse.model.dto.chips.xml.ChipsPresenterDetails;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChipsFormDataMapper {

    private static final int FORM_VERSION = 1;
    private static final int PACKAGE_COUNT = 1;
    private static final String FILING_DETAILS_METHOD = "enablement";

    private static final String CHIPS_DATE_FORMAT = "yyyy-MM-dd";

    private final XmlMapper xmlMapper;

    @Autowired
    public ChipsFormDataMapper(@Qualifier("xmlMapper") XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    public String mapToChipsFormDataXml(Dissolution dissolution) {
        final ChipsFormData form = new ChipsFormData();

        form.setType(ChipsFormType.findByApplicationType(dissolution.getData().getApplication().getType()));
        form.setVersion(FORM_VERSION);
        form.setFilingDetails(mapToFilingDetails(dissolution));
        form.setCorporateBody(mapToCorporateBody(dissolution));

        return toXml(form);
    }

    private ChipsFilingDetails mapToFilingDetails(Dissolution dissolution) {
        final ChipsFilingDetails filingDetails = new ChipsFilingDetails();

        final String reference = dissolution.getData().getApplication().getReference();

        filingDetails.setPresenterDocumentReference(reference);
        filingDetails.setPresenterDetails(mapToPresenterDetails(dissolution));
        filingDetails.setReceiptDate(asDateString(dissolution.getPaymentInformation().getDateTime()));
        filingDetails.setSignDate(asDateString(dissolution.getCreatedBy().getDateTime()));
        filingDetails.setSubmissionReference(reference);
        filingDetails.setPayment(mapToPayment(dissolution));
        filingDetails.setBarcode(dissolution.getData().getApplication().getBarcode());
        filingDetails.setPackageIdentifier(reference);
        filingDetails.setPackageCount(PACKAGE_COUNT);
        filingDetails.setMethod(FILING_DETAILS_METHOD);

        return filingDetails;
    }

    private ChipsPresenterDetails mapToPresenterDetails(Dissolution dissolution) {
        final ChipsPresenterDetails presenterDetails = new ChipsPresenterDetails();

        final String email = dissolution.getCreatedBy().getEmail();

        presenterDetails.setPresenterEmailIn(email);
        presenterDetails.setPresenterEmailOut(email);

        return presenterDetails;
    }

    private ChipsPayment mapToPayment(Dissolution dissolution) {
        final ChipsPayment payment = new ChipsPayment();

        final PaymentInformation dissolutionPayment = dissolution.getPaymentInformation();

        payment.setPaymentMethod(ChipsPaymentMethod.findByDissolutionPaymentMethod(dissolutionPayment.getMethod()));

        if (dissolutionPayment.getMethod() == PaymentMethod.ACCOUNT) {
            payment.setAccountNumber(dissolutionPayment.getAccountNumber());
        } else {
            payment.setReferenceNumber(dissolutionPayment.getReference());
        }

        return payment;
    }

    private ChipsCorporateBody mapToCorporateBody(Dissolution dissolution) {
        final ChipsCorporateBody corporateBody = new ChipsCorporateBody();

        final Company company = dissolution.getCompany();

        corporateBody.setCorporateBodyName(company.getName());
        corporateBody.setIncorporationNumber(company.getNumber());
        corporateBody.setOfficers(mapToOfficers(dissolution));

        return corporateBody;
    }

    private List<ChipsOfficer> mapToOfficers(Dissolution dissolution) {
        return dissolution.getData().getDirectors().stream().map(this::mapToOfficer).collect(Collectors.toList());
    }

    private ChipsOfficer mapToOfficer(DissolutionDirector director) {
        final ChipsOfficer officer = new ChipsOfficer();

        officer.setPersonName(mapToPersonName(director.getName()));
        officer.setSignDate(asDateString(director.getDirectorApproval().getDateTime()));
        officer.setEmail(director.getEmail());
        officer.setIpAddress(director.getDirectorApproval().getIpAddress());

        Optional.ofNullable(director.getOnBehalfName()).ifPresent(officer::setOnBehalfName);

        return officer;
    }

    private ChipsPersonName mapToPersonName(String name) {
        final ChipsPersonName personName = new ChipsPersonName();
        final int nameSeparatorIndex = name.indexOf(',');

        if (nameSeparatorIndex == -1) {
            personName.setSurname(name.trim());
        } else {
            personName.setForename(name.substring(nameSeparatorIndex + 1).trim());
            personName.setSurname(name.substring(0, nameSeparatorIndex).trim());
        }

        return personName;
    }

    private String asDateString(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(CHIPS_DATE_FORMAT));
    }

    private String toXml(ChipsFormData form) {
        try {
            return xmlMapper.writeValueAsString(form);
        } catch (JsonProcessingException ex) {
            throw new ChipsMapperException(String.format("Failed to map to CHIPS request for company %s", form.getCorporateBody().getIncorporationNumber()), ex);
        }
    }
}
