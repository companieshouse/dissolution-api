package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DissolutionCreatorTest {

    @InjectMocks
    private DissolutionCreator creator;

    @Mock
    ReferenceGenerator referenceGenerator;

    @Mock
    DissolutionRequestMapper requestMapper;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionResponseMapper responseMapper;

    @Test
    public void create_generatesAReferenceNumber_mapsToDissolution_savesInDatabase_returnsCreateResponse() throws Exception {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final String companyNumber = "12345678";
        final String userId = "123";
        final String ip = "192.168.0.1";
        final String email = "user@mail.com";

        final String reference = "ABC123";
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionCreateResponse response = DissolutionFixtures.generateDissolutionCreateResponse();

        when(referenceGenerator.generateApplicationReference()).thenReturn(reference);
        when(requestMapper.mapToDissolution(body, companyNumber, userId, email, ip, reference)).thenReturn(dissolution);
        when(responseMapper.mapToDissolutionCreateResponse(dissolution)).thenReturn(response);

        final DissolutionCreateResponse result = creator.create(body, companyNumber, userId, ip, email);

        verify(referenceGenerator).generateApplicationReference();
        verify(requestMapper).mapToDissolution(body, companyNumber, userId, email, ip, reference);
        verify(responseMapper).mapToDissolutionCreateResponse(dissolution);

        assertEquals(response, result);
    }
}
