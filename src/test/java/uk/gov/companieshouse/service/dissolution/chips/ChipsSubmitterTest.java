package uk.gov.companieshouse.service.dissolution.chips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.mapper.chips.DissolutionChipsMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateDownloader;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.ChipsFixtures.generateDissolutionChipsRequest;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;

@ExtendWith(MockitoExtension.class)
public class ChipsSubmitterTest {

    @InjectMocks
    private ChipsSubmitter submitter;

    @Mock
    private DissolutionCertificateDownloader certificateDownloader;

    @Mock
    private DissolutionChipsMapper mapper;

    @Mock
    private ChipsClient client;

    @Test
    public void submitDissolutionToChips_downloadsCertificate_mapsToRequest_sendsToChips() throws Exception {
        final Dissolution dissolution = generateDissolution();
        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        when(certificateDownloader.downloadDissolutionCertificate(dissolution)).thenReturn("some certificate contents");
        when(mapper.mapToDissolutionChipsRequest(dissolution, "some certificate contents")).thenReturn(request);

        submitter.submitDissolutionToChips(dissolution);

        verify(certificateDownloader).downloadDissolutionCertificate(dissolution);
        verify(mapper).mapToDissolutionChipsRequest(dissolution, "some certificate contents");
        verify(client).sendDissolutionToChips(request);
    }

    @Test
    public void submitDissolutionToChips_updatesDatabase_ifChipsSubmissionSucceeds() throws Exception {
        // TODO
    }

    @Test
    public void submitDissolutionToChips_updatesDatabase_ifChipsSubmissionFails() throws Exception {
        // TODO
    }
}
