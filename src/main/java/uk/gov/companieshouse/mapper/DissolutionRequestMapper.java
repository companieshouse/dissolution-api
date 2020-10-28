package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.model.db.dissolution.Company;
import uk.gov.companieshouse.model.db.dissolution.CreatedBy;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionApplication;
import uk.gov.companieshouse.model.db.dissolution.DissolutionData;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.domain.DissolutionUserData;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.CompanyType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.companieshouse.util.DateTimeGenerator.generateCurrentDateTime;

@Service
public class DissolutionRequestMapper {

    public Dissolution mapToDissolution(DissolutionCreateRequest body, CompanyProfile company, Map<String, CompanyOfficer> directors, DissolutionUserData userData, String reference, String barcode) {
        final Dissolution dissolution = new Dissolution();

        dissolution.setModifiedDateTime(generateCurrentDateTime());
        dissolution.setData(mapToDissolutionData(body, company.getType(), directors, reference, barcode));
        dissolution.setCompany(mapToCompany(company.getCompanyNumber(), company.getCompanyName()));
        dissolution.setCreatedBy(mapToCreatedBy(userData.getUserId(), userData.getEmail(), userData.getIpAddress()));
        dissolution.setActive(true);

        return dissolution;
    }

    private DissolutionData mapToDissolutionData(DissolutionCreateRequest body, String companyType, Map<String, CompanyOfficer> directors, String reference, String barcode) {
        final DissolutionData data = new DissolutionData();

        data.setETag(GenerateEtagUtil.generateEtag());
        data.setApplication(mapToDissolutionApplication(companyType, reference, barcode));
        data.setDirectors(mapToDissolutionDirectors(body.getDirectors(), directors));

        return data;
    }

    private DissolutionApplication mapToDissolutionApplication(String companyType, String reference, String barcode) {
        final DissolutionApplication application = new DissolutionApplication();

        application.setBarcode(barcode);
        application.setReference(reference);
        application.setStatus(ApplicationStatus.PENDING_APPROVAL);
        application.setType(companyType.equals(CompanyType.LLP.getValue()) ? ApplicationType.LLDS01 : ApplicationType.DS01);

        return application;
    }

    private List<DissolutionDirector> mapToDissolutionDirectors(List<DirectorRequest> selectedDirectors, Map<String, CompanyOfficer> companyDirectors) {
        return selectedDirectors.stream().map(selectedDirector -> mapToDissolutionDirector(selectedDirector, companyDirectors)).collect(Collectors.toList());
    }

    private DissolutionDirector mapToDissolutionDirector(DirectorRequest selectedDirector, Map<String, CompanyOfficer> companyDirectors) {
        final DissolutionDirector director = new DissolutionDirector();

        director.setOfficerId(selectedDirector.getOfficerId());
        director.setEmail(selectedDirector.getEmail().toLowerCase());
        director.setOnBehalfName(selectedDirector.getOnBehalfName());
        director.setName(getSelectedDirectorName(selectedDirector, companyDirectors));

        return director;
    }

    private String getSelectedDirectorName(DirectorRequest selectedDirector, Map<String, CompanyOfficer> companyDirectors) {
        return companyDirectors.get(selectedDirector.getOfficerId()).getName();
    }

    private Company mapToCompany(String companyNumber, String companyName) {
        final Company company = new Company();

        company.setNumber(companyNumber);
        company.setName(companyName);

        return company;
    }

    private CreatedBy mapToCreatedBy(String userId, String email, String ip) {
        final CreatedBy createdBy = new CreatedBy();

        createdBy.setUserId(userId);
        createdBy.setEmail(email);
        createdBy.setIpAddress(ip);
        createdBy.setDateTime(generateCurrentDateTime());

        return createdBy;
    }
}
