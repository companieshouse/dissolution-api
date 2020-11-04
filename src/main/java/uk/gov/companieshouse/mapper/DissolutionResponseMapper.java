package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.dissolution.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.companieshouse.model.Constants.DISSOLUTION_KIND;

@Service
public class DissolutionResponseMapper {

    public DissolutionCreateResponse mapToDissolutionCreateResponse(Dissolution dissolution) {
        final DissolutionCreateResponse response = new DissolutionCreateResponse();

        final String reference = dissolution.getData().getApplication().getReference();

        response.setApplicationReferenceNumber(reference);
        response.setLinks(generateLinks(dissolution.getCompany().getNumber(), reference));

        return response;
    }

    public DissolutionGetResponse mapToDissolutionGetResponse(Dissolution dissolution) {
        final DissolutionGetResponse response = new DissolutionGetResponse();

        final String reference = dissolution.getData().getApplication().getReference();

        response.setETag(dissolution.getData().getETag());
        response.setKind(DISSOLUTION_KIND);
        response.setLinks(generateLinks(dissolution.getCompany().getNumber(), reference));
        response.setApplicationStatus(dissolution.getData().getApplication().getStatus());
        response.setApplicationReference(reference);
        response.setApplicationType(dissolution.getData().getApplication().getType());
        response.setCompanyName(dissolution.getCompany().getName());
        response.setCompanyNumber(dissolution.getCompany().getNumber());
        response.setCreatedAt(Timestamp.valueOf(dissolution.getCreatedBy().getDateTime()));
        response.setCreatedBy(dissolution.getCreatedBy().getEmail());
        response.setDirectors(mapToDissolutionGetDirectors(dissolution.getData().getDirectors()));

        Optional
                .ofNullable(dissolution.getCertificate())
                .ifPresent(certificate -> setCertificateDetails(response, certificate));

        return response;
    }

    public DissolutionPatchResponse mapToDissolutionPatchResponse(Dissolution dissolution) {
        final DissolutionPatchResponse response = new DissolutionPatchResponse();

        response.setLinks(generateLinks(
                dissolution.getCompany().getNumber(),
                dissolution.getData().getApplication().getReference()
        ));

        return response;
    }

    private DissolutionLinks generateLinks(String companyNumber, String reference) {
        final DissolutionLinks links = new DissolutionLinks();

        links.setSelf(String.format("/dissolution-request/%s", companyNumber));
        links.setPayment(String.format("/dissolution-request/%s/payment", reference));

        return links;
    }

    private List<DissolutionGetDirector> mapToDissolutionGetDirectors(List<DissolutionDirector> directors) {
        return directors.stream().map(this::mapToDissolutionGetDirector).collect(Collectors.toList());
    }

    private DissolutionGetDirector mapToDissolutionGetDirector(DissolutionDirector director) {
        DissolutionGetDirector getDirector = new DissolutionGetDirector();

        getDirector.setOfficerId(director.getOfficerId());
        getDirector.setName(director.getName());
        getDirector.setEmail(director.getEmail());
        getDirector.setOnBehalfName(director.getOnBehalfName());

        Optional
                .ofNullable(director.getDirectorApproval())
                .ifPresent(approval -> getDirector.setApprovedAt(Timestamp.valueOf(approval.getDateTime())));

        return getDirector;
    }

    private void setCertificateDetails(DissolutionGetResponse response, DissolutionCertificate certificate) {
        response.setCertificateBucket(certificate.getBucket());
        response.setCertificateKey(certificate.getKey());
    }
}
