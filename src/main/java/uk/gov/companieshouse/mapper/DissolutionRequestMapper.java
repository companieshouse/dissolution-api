package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.*;
import uk.gov.companieshouse.model.dto.CreateDissolutionRequestDTO;
import uk.gov.companieshouse.model.dto.DirectorRequestDTO;
import uk.gov.companieshouse.GenerateEtagUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DissolutionRequestMapper {

    public Dissolution mapToDissolution(CreateDissolutionRequestDTO body, String companyNumber, String userId, String email, String ip, String reference) {
        return new Dissolution() {{
            setModifiedDateTime(LocalDateTime.now());
            setData(mapToDissolutionData(body, reference));
            setCompany(mapToCompany(companyNumber));
            setCreatedBy(mapToCreatedBy(userId, email, ip));
        }};
    }

    private DissolutionData mapToDissolutionData(CreateDissolutionRequestDTO body, String reference) {
        return new DissolutionData() {{
            setETag(GenerateEtagUtil.generateEtag());
            setApplication(mapToDissolutionApplication(reference));
            setDirectors(mapToDissolutionDirectors(body.getDirectors()));
        }};
    }

    private DissolutionApplication mapToDissolutionApplication(String reference) {
        return new DissolutionApplication() {{
            setReference(reference);
            setStatus(DissolutionApplication.DissolutionStatus.PENDING_APPROVAL);
            setType(DissolutionApplication.DissolutionType.DS01);
        }};
    }

    private List<DissolutionDirector> mapToDissolutionDirectors(List<DirectorRequestDTO> directors) {
        return directors.stream().map(this::mapToDissolutionDirector).collect(Collectors.toList());
    }

    private DissolutionDirector mapToDissolutionDirector(DirectorRequestDTO body) {
        return new DissolutionDirector() {{
            setName(body.getName());
            setEmail(body.getEmail());
            Optional.ofNullable(body.getOnBehalfName()).ifPresent(this::setOnBehalfName);
        }};
    }

    private Company mapToCompany(String companyNumber) {
        return new Company() {{
            setNumber(companyNumber);
            setName("PLACEHOLDER COMPANY NAME"); // TODO - replace with actual name once Company Profile API integration is added
        }};
    }

    private CreatedBy mapToCreatedBy(String userId, String email, String ip) {
        return new CreatedBy() {{
            setUserId(userId);
            setEmail(email);
            setIpAddress(ip);
            setDateTime(LocalDateTime.now());
        }};
    }
}
