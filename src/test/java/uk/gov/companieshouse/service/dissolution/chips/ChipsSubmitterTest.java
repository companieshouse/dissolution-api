package uk.gov.companieshouse.service.dissolution.chips;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.config.ChipsConfig;
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

    private static final byte[] CERTIFICATE_CONTENTS = "some certificate contents".getBytes();

    @InjectMocks
    private ChipsSubmitter submitter;

    @Mock
    private DissolutionCertificateDownloader certificateDownloader;

    @Mock
    private DissolutionChipsMapper mapper;

    @Mock
    private ChipsClient client;

    @Mock
    private ChipsConfig config;

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
    public void submitDissolutionToChips_downloadsCertificate_mapsToRequest_sendsToChips() {
        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        when(certificateDownloader.downloadDissolutionCertificate(dissolution)).thenReturn(CERTIFICATE_CONTENTS);
        when(mapper.mapToDissolutionChipsRequest(dissolution, CERTIFICATE_CONTENTS)).thenReturn(request);

        submitter.submitDissolutionToChips(dissolution);

        verify(certificateDownloader).downloadDissolutionCertificate(dissolution);
        verify(mapper).mapToDissolutionChipsRequest(dissolution, CERTIFICATE_CONTENTS);
        verify(client).sendDissolutionToChips(request);
    }

    @Test
    public void submitDissolutionToChips_updatesDatabase_ifChipsSubmissionSucceeds() {
        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        when(certificateDownloader.downloadDissolutionCertificate(dissolution)).thenReturn(CERTIFICATE_CONTENTS);
        when(mapper.mapToDissolutionChipsRequest(dissolution, CERTIFICATE_CONTENTS)).thenReturn(request);

        submitter.submitDissolutionToChips(dissolution);

        verify(repository).save(dissolutionCaptor.capture());

        final Dissolution updatedDissolution = dissolutionCaptor.getValue();

        assertNotNull(updatedDissolution.getSubmission().getDateTime());
        assertEquals(SubmissionStatus.SENT, updatedDissolution.getSubmission().getStatus());
    }

    @Test
    public void submitDissolutionToChips_updatesDatabase_ifChipsSubmissionFails_andRetriesNotExceeded() {
        dissolution.getSubmission().setRetryCounter(1);

        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        when(certificateDownloader.downloadDissolutionCertificate(dissolution)).thenReturn(CERTIFICATE_CONTENTS);
        when(mapper.mapToDissolutionChipsRequest(dissolution, CERTIFICATE_CONTENTS)).thenReturn(request);
        doThrow(new ChipsNotAvailableException()).when(client).sendDissolutionToChips(request);
        when(config.getChipsRetryLimit()).thenReturn(10);

        submitter.submitDissolutionToChips(dissolution);

        verify(repository).save(dissolutionCaptor.capture());

        final Dissolution updatedDissolution = dissolutionCaptor.getValue();

        assertNotNull(updatedDissolution.getSubmission().getDateTime());
        assertEquals(SubmissionStatus.PENDING, updatedDissolution.getSubmission().getStatus());
        assertEquals(2, updatedDissolution.getSubmission().getRetryCounter());
    }

    @Test
    public void submitDissolutionToChips_updatesDatabase_ifChipsSubmissionFails_andRetriesExceeded() {
        dissolution.getSubmission().setRetryCounter(9);

        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        when(certificateDownloader.downloadDissolutionCertificate(dissolution)).thenReturn(CERTIFICATE_CONTENTS);
        when(mapper.mapToDissolutionChipsRequest(dissolution, CERTIFICATE_CONTENTS)).thenReturn(request);
        doThrow(new ChipsNotAvailableException()).when(client).sendDissolutionToChips(request);
        when(config.getChipsRetryLimit()).thenReturn(10);

        submitter.submitDissolutionToChips(dissolution);

        verify(repository).save(dissolutionCaptor.capture());

        final Dissolution updatedDissolution = dissolutionCaptor.getValue();

        assertNotNull(updatedDissolution.getSubmission().getDateTime());
        assertEquals(SubmissionStatus.FAILED, updatedDissolution.getSubmission().getStatus());
        assertEquals(10, updatedDissolution.getSubmission().getRetryCounter());
    }
}
