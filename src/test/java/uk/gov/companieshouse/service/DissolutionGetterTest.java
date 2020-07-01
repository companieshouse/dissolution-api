package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DissolutionGetterTest {

    @InjectMocks
    private DissolutionGetter getter;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionResponseMapper responseMapper;

    @Test
    public void get_findsDissolution_mapsToDissolutionResponse_returnsGetResponse() {
        final String companyNumber = "12345678";
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(repository.findByCompanyNumber(companyNumber)).thenReturn(Optional.of(dissolution));
        when(responseMapper.mapToDissolutionGetResponse(dissolution)).thenReturn(response);

        final Optional<DissolutionGetResponse> result = getter.get(companyNumber);

        verify(repository).findByCompanyNumber(companyNumber);
        verify(responseMapper).mapToDissolutionGetResponse(dissolution);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }
}
