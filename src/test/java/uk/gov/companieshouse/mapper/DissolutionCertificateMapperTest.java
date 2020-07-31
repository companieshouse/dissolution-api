package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.documentRender.DissolutionCertificateData;
import uk.gov.companieshouse.model.dto.documentRender.DissolutionCertificateDirector;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;

public class DissolutionCertificateMapperTest {

    final DissolutionCertificateMapper mapper = new DissolutionCertificateMapper();

    @Test
    public void mapToCertificateData_setsCompanyNameAndNumber_fromDissolution() {
        final Dissolution dissolution = generateDissolution();
        dissolution.getCompany().setName("some company");
        dissolution.getCompany().setNumber("12345");
        dissolution.getData().setDirectors(Collections.emptyList());

        final DissolutionCertificateData result = mapper.mapToCertificateData(dissolution);

        assertEquals("some company", result.getCompanyName());
        assertEquals("12345", result.getCompanyNumber());
    }

    @Test
    public void mapToCertificateData_setsDirectorInformation_fromDissolution() {
        final Dissolution dissolution = generateDissolution();

        final DirectorApproval approvalOne = generateDirectorApproval();
        approvalOne.setDateTime(LocalDateTime.of(2020, 10, 20, 0, 0));

        final DissolutionDirector directorOne = generateDissolutionDirector();
        directorOne.setName("Director One");
        directorOne.setOnBehalfName(null);
        directorOne.setDirectorApproval(approvalOne);

        final DirectorApproval approvalTwo = generateDirectorApproval();
        approvalTwo.setDateTime(LocalDateTime.of(2019, 9, 19, 0, 0));

        final DissolutionDirector directorTwo = generateDissolutionDirector();
        directorTwo.setName("Director Two");
        directorTwo.setOnBehalfName("Some on behalf name");
        directorTwo.setDirectorApproval(approvalTwo);

        dissolution.getData().setDirectors(Arrays.asList(directorOne, directorTwo));

        final DissolutionCertificateData result = mapper.mapToCertificateData(dissolution);

        assertEquals(2, result.getDirectors().size());

        final DissolutionCertificateDirector certificateDirector1 = result.getDirectors().get(0);
        assertEquals("Director One", certificateDirector1.getName());
        assertEquals("20-10-2020", certificateDirector1.getApprovalDate());
        assertNull(certificateDirector1.getOnBehalfName());

        final DissolutionCertificateDirector certificateDirector2 = result.getDirectors().get(1);
        assertEquals("Director Two", certificateDirector2.getName());
        assertEquals("19-09-2019", certificateDirector2.getApprovalDate());
        assertEquals("Some on behalf name", certificateDirector2.getOnBehalfName());
    }

    @Test
    public void mapToDissolutionCertificate_extractsBucketAndKeyFromLocationUrl() {
        final String location = "s3://some-bucket/some-env/some-file.pdf";

        final DissolutionCertificate result = mapper.mapToDissolutionCertificate(location);

        assertEquals("some-bucket", result.getBucket());
        assertEquals("some-env/some-file.pdf", result.getKey());
    }
}