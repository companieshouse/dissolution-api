package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompanyProfileMapperTest {

    private final CompanyProfileMapper mapper = Mappers.getMapper(CompanyProfileMapper.class);

    @Test
    void mapToCompanyProfile_mapsAllFields() {
        final CompanyProfileApi api = new CompanyProfileApi();
        api.setCompanyName("Test Company");
        api.setType("ltd");
        api.setCompanyNumber("12345678");
        api.setCompanyStatus("active");

        final CompanyProfile result = mapper.mapToCompanyProfile(api);

        assertEquals("Test Company", result.companyName());
        assertEquals("ltd", result.type());
        assertEquals("12345678", result.companyNumber());
        assertEquals("active", result.companyStatus());
    }

    @Test
    void mapToCompanyProfile_whenNull_returnsNull() {
        assertNull(mapper.mapToCompanyProfile(null));
    }
}
