package uk.gov.companieshouse.service.dissolution.chips;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.exception.ChipsNotAvailableException;
import uk.gov.companieshouse.mapper.chips.DissolutionChipsMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;
import uk.gov.companieshouse.model.enums.SubmissionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateDownloader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.fixtures.ChipsFixtures.generateDissolutionChipsRequest;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionSubmission;

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

    @Mock
    private DissolutionRepository repository;

    private Dissolution dissolution;
    private ArgumentCaptor<Dissolution> dissolutionCaptor;

    @BeforeEach
    public void setup() {
        dissolution = generateDissolution();

        DissolutionSubmission submission = generateDissolutionSubmission();
        submission.setStatus(SubmissionStatus.PENDING);
        dissolution.setSubmission(submission);

        dissolutionCaptor = ArgumentCaptor.forClass(Dissolution.class);
    }

    @Test
    public void submitDissolutionToChips_downloadsCertificate_mapsToRequest_sendsToChips() throws Exception {
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
        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        when(certificateDownloader.downloadDissolutionCertificate(dissolution)).thenReturn("some certificate contents");
        when(mapper.mapToDissolutionChipsRequest(dissolution, "some certificate contents")).thenReturn(request);

        submitter.submitDissolutionToChips(dissolution);

        verify(repository).save(dissolutionCaptor.capture());

        final Dissolution updatedDissolution = dissolutionCaptor.getValue();

        assertNotNull(updatedDissolution.getSubmission().getDateTime());
        assertEquals(SubmissionStatus.SENT, updatedDissolution.getSubmission().getStatus());
    }

    @Test
    public void submitDissolutionToChips_updatesDatabase_ifChipsSubmissionFails_andRetriesNotExceeded() throws Exception {
        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        when(certificateDownloader.downloadDissolutionCertificate(dissolution)).thenReturn("some certificate contents");
        when(mapper.mapToDissolutionChipsRequest(dissolution, "some certificate contents")).thenReturn(request);
        doThrow(new ChipsNotAvailableException()).when(client).sendDissolutionToChips(request);

        submitter.submitDissolutionToChips(dissolution);

        verify(repository).save(dissolutionCaptor.capture());

        final Dissolution updatedDissolution = dissolutionCaptor.getValue();

        assertNotNull(updatedDissolution.getSubmission().getDateTime());
        assertEquals(SubmissionStatus.PENDING, updatedDissolution.getSubmission().getStatus());
        // TODO - increment retry counter
    }

    @Test
    public void submitDissolutionToChips_updatesDatabase_ifChipsSubmissionFails_andRetriesExceeded() throws Exception {
        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        when(certificateDownloader.downloadDissolutionCertificate(dissolution)).thenReturn("some certificate contents");
        when(mapper.mapToDissolutionChipsRequest(dissolution, "some certificate contents")).thenReturn(request);
        doThrow(new ChipsNotAvailableException()).when(client).sendDissolutionToChips(request);

        submitter.submitDissolutionToChips(dissolution);

        verify(repository).save(dissolutionCaptor.capture());

        final Dissolution updatedDissolution = dissolutionCaptor.getValue();

        assertNotNull(updatedDissolution.getSubmission().getDateTime());
        // TODO - assert status is failed
        // TODO - increment retry counter
    }
}
