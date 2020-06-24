package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DissolutionServiceTest {

    @InjectMocks
    private DissolutionService service;

    @Mock
    private DissolutionCreator creator;

    @Mock
    private DissolutionRepository repository;

    @Test
    public void create_createsADissolutionRequest_returnsCreateResponse() throws Exception {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final String companyNumber = "12345678";
        final String userId = "123";
        final String ip = "192.168.0.1";
        final String email = "user@mail.com";

        final DissolutionCreateResponse response = DissolutionFixtures.generateDissolutionCreateResponse();

        when(creator.create(body, companyNumber, userId, ip, email)).thenReturn(response);

        final DissolutionCreateResponse result = service.create(body, companyNumber, userId, ip, email);

        verify(creator).create(body, companyNumber, userId, ip, email);

        assertEquals(response, result);
    }

    @Test
    public void doesDissolutionRequestExistForCompany_returnsTrue_ifDissolutionForCompanyExists() {
        final String companyNumber = "1234";

        when(repository.findByCompanyNumber(companyNumber)).thenReturn(Optional.of(DissolutionFixtures.generateDissolution()));

        final boolean result = service.doesDissolutionRequestExistForCompany(companyNumber);

        assertTrue(result);
    }

    @Test
    public void doesDissolutionRequestExistForCompany_returnsFalse_ifDissolutionForCompanyDoesNotExist() {
        final String companyNumber = "1234";

        when(repository.findByCompanyNumber(companyNumber)).thenReturn(Optional.empty());

        final boolean result = service.doesDissolutionRequestExistForCompany(companyNumber);

        assertFalse(result);
    }
}
