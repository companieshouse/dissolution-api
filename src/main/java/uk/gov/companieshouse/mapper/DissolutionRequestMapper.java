package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.model.db.*;
import uk.gov.companieshouse.model.dto.DirectorRequest;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DissolutionRequestMapper {

    public Dissolution mapToDissolution(DissolutionCreateRequest body, String companyNumber, String userId, String email, String ip, String reference) {
        final Dissolution dissolution = new Dissolution();

        dissolution.setModifiedDateTime(LocalDateTime.now());
        dissolution.setData(mapToDissolutionData(body, reference));
        dissolution.setCompany(mapToCompany(companyNumber));
        dissolution.setCreatedBy(mapToCreatedBy(userId, email, ip));

        return dissolution;
    }

    private DissolutionData mapToDissolutionData(DissolutionCreateRequest body, String reference) {
        final DissolutionData data = new DissolutionData();

        data.setETag(GenerateEtagUtil.generateEtag());
        data.setApplication(mapToDissolutionApplication(reference));
        data.setDirectors(mapToDissolutionDirectors(body.getDirectors()));

        return data;
    }

    private DissolutionApplication mapToDissolutionApplication(String reference) {
        final DissolutionApplication application = new DissolutionApplication();

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

    private Company mapToCompany(String companyNumber) {
        final Company company = new Company();

        company.setNumber(companyNumber);
        company.setName("PLACEHOLDER COMPANY NAME"); // TODO - replace with actual name once Company Profile API integration is added

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