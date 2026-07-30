package uk.gov.companieshouse.mapper.filing;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.exception.FilingDataMapperException;
import uk.gov.companieshouse.model.db.dissolution.Company;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.filing.FilingData;
import uk.gov.companieshouse.model.dto.filing.FilingOfficer;
import uk.gov.companieshouse.model.dto.filing.FilingPersonName;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FilingDataMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String ERROR_MESSAGE = "Failed to map to filing data for company %s with dissolution %s";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public FilingDataMapper(@Qualifier("jacksonJsonMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> mapToFilingData(Dissolution dissolution, String paymentReference, String paymentMethod) {
        final FilingData data = new FilingData();
        final Company company = dissolution.getCompany();

        data.setCompanyName(company.getName());
        data.setCompanyNumber(company.getNumber());
        data.setOfficers(mapToOfficers(dissolution));
        data.setPaymentReference(paymentReference);
        data.setPaymentMethod(paymentMethod);

        try {
            return objectMapper.convertValue(data, MAP_TYPE);
        } catch (Exception e) {
            throw new FilingDataMapperException(String.format(ERROR_MESSAGE, company.getNumber(), dissolution.getId()), e);
        }
    }

    private List<FilingOfficer> mapToOfficers(Dissolution dissolution) {
        return dissolution.getData().getDirectors().stream().map(this::mapToOfficer).toList();
    }

    private FilingOfficer mapToOfficer(DissolutionDirector director) {
        final FilingOfficer officer = new FilingOfficer();

        officer.setPersonName(mapToPersonName(director.getName()));
        officer.setSignDate(director.getDirectorApproval().getDateTime().format(DATE_FORMATTER));
        officer.setEmail(director.getEmail());
        officer.setIpAddress(director.getDirectorApproval().getIpAddress());
        Optional.ofNullable(director.getOnBehalfName()).ifPresent(officer::setOnBehalfName);

        return officer;
    }

    private FilingPersonName mapToPersonName(String name) {
        final FilingPersonName personName = new FilingPersonName();
        final int separatorIndex = name.indexOf(',');

        if (separatorIndex == -1) {
            personName.setSurname(name.trim());
        } else {
            personName.setForename(name.substring(separatorIndex + 1).trim());
            personName.setSurname(name.substring(0, separatorIndex).trim());
        }

        return personName;
    }

    private static ObjectMapper buildMapper() {
        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }
}
