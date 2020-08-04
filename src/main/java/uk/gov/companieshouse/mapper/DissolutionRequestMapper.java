package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.model.CompanyProfile;
import uk.gov.companieshouse.model.db.dissolution.*;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DissolutionRequestMapper {

    public Dissolution mapToDissolution(DissolutionCreateRequest body, CompanyProfile company, String userId, String email, String ip, String reference, String barcode) {
        final Dissolution dissolution = new Dissolution();

        dissolution.setModifiedDateTime(LocalDateTime.now());
        dissolution.setData(mapToDissolutionData(body, reference, barcode));
        dissolution.setCompany(mapToCompany(company.getCompanyNumber(), company.getCompanyName()));
        dissolution.setCreatedBy(mapToCreatedBy(userId, email, ip));

        return dissolution;
    }

    private DissolutionData mapToDissolutionData(DissolutionCreateRequest body, String reference, String barcode) {
        final DissolutionData data = new DissolutionData();

        data.setETag(GenerateEtagUtil.generateEtag());
        data.setApplication(mapToDissolutionApplication(reference, barcode));
        data.setDirectors(mapToDissolutionDirectors(body.getDirectors()));

        return data;
    }

    private DissolutionApplication mapToDissolutionApplication(String reference, String barcode) {
        final DissolutionApplication application = new DissolutionApplication();

        application.setBarcode(barcode);
        application.setReference(reference);
        application.setStatus(ApplicationStatus.PENDING_APPROVAL);
        application.setType(ApplicationType.DS01);

        return application;
    }

    private List<DissolutionDirector> mapToDissolutionDirectors(List<DirectorRequest> directors) {
        return directors.stream().map(this::mapToDissolutionDirector).collect(Collectors.toList());
    }

    private DissolutionDirector mapToDissolutionDirector(DirectorRequest body) {
        final DissolutionDirector director = new DissolutionDirector();

        director.setName(body.getName());
        director.setEmail(body.getEmail());
        director.setOnBehalfName(body.getOnBehalfName());

        return director;
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
        createdBy.setDateTime(LocalDateTime.now());

        return createdBy;
    }
}
