package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionGetter;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    public static final String COMPANY_NUMBER = "12345678";
    public static final String EMAIL = "user@mail.com";

    @Test
    public void get_findsDissolution_mapsToDissolutionResponse_returnsGetResponse() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(responseMapper.mapToDissolutionGetResponse(dissolution)).thenReturn(response);

        final Optional<DissolutionGetResponse> result = getter.get(COMPANY_NUMBER);

        verify(repository).findByCompanyNumber(COMPANY_NUMBER);
        verify(responseMapper).mapToDissolutionGetResponse(dissolution);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    public void get_doesNotFindDissolution_returnsOptionalEmpty() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final Optional<DissolutionGetResponse> result = getter.get(COMPANY_NUMBER);

        verify(repository).findByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result.isEmpty());
    }

    @Test
    public void mapToDirectorPendingApproval_mapDirectorToBoolean_returnFalse_whenEmailNotFound() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean approved = getter.isDirectorPendingApproval(COMPANY_NUMBER, EMAIL);

        assertFalse(approved);
    }

    @Test
    public void mapToDirectorPendingApproval_mapDirectorToBoolean_returnTrue_whenNotApproved() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final List<DissolutionDirector> dissolutionDirectorList = dissolution.getData().getDirectors();
        final DissolutionDirector director = dissolutionDirectorList.get(0);
        director.setEmail(EMAIL);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean approved = getter.isDirectorPendingApproval(COMPANY_NUMBER, EMAIL);

        assertTrue(approved);
    }

    @Test
    public void mapToDirectorPendingApproval_mapDirectorToBoolean_returnFalse_whenApproved() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final List<DissolutionDirector> dissolutionDirectorList = dissolution.getData().getDirectors();
        final DirectorApproval approval = DissolutionFixtures.generateDirectorApproval();
        final DissolutionDirector director = dissolutionDirectorList.get(0);
        director.setDirectorApproval(approval);
        director.setEmail(EMAIL);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean approved = getter.isDirectorPendingApproval(COMPANY_NUMBER, EMAIL);

        assertFalse(approved);
    }
}
