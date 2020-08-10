package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.DocumentRenderConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.documentRender.DissolutionCertificateData;
import uk.gov.companieshouse.model.dto.documentRender.DissolutionCertificateDirector;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DissolutionCertificateMapper {

    private static final String APPROVAL_DATE_FORMAT = "dd-MM-yyyy";

    private final DocumentRenderConfig config;

    public DissolutionCertificateMapper(DocumentRenderConfig config) {
        this.config = config;
    }

    public DissolutionCertificateData mapToCertificateData(Dissolution dissolution) {
        final DissolutionCertificateData data = new DissolutionCertificateData();

        data.setCdn(config.getCdnHost());
        data.setCompanyName(dissolution.getCompany().getName());
        data.setCompanyNumber(dissolution.getCompany().getNumber());
        data.setDirectors(mapToCertificateDirectors(dissolution.getData().getDirectors()));

        return data;
    }

    public DissolutionCertificate mapToDissolutionCertificate(String location) {
        final URI locationUri = mapToURI(location);

        final DissolutionCertificate certificate = new DissolutionCertificate();

        certificate.setBucket(locationUri.getHost());
        certificate.setKey(locationUri.getPath().substring(1));

        return certificate;
    }

    private List<DissolutionCertificateDirector> mapToCertificateDirectors(List<DissolutionDirector> directors) {
        return directors.stream().map(this::mapToCertificateDirector).collect(Collectors.toList());
    }

    private DissolutionCertificateDirector mapToCertificateDirector(DissolutionDirector director) {
        final DissolutionCertificateDirector certificateDirector = new DissolutionCertificateDirector();

        certificateDirector.setName(director.getName());
        certificateDirector.setApprovalDate(getFormattedApprovalDate(director));
        Optional.ofNullable(director.getOnBehalfName()).ifPresent(certificateDirector::setOnBehalfName);

        return certificateDirector;
    }

    private String getFormattedApprovalDate(DissolutionDirector director) {
        return director.getDirectorApproval().getDateTime().format(DateTimeFormatter.ofPattern(APPROVAL_DATE_FORMAT));
    }

    private URI mapToURI(String location) {
        try {
            return new URI(location);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
