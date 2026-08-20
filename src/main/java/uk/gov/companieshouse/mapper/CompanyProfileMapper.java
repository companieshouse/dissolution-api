package uk.gov.companieshouse.mapper;

import org.mapstruct.Mapper;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;

@Mapper(componentModel = "spring")
public interface CompanyProfileMapper {
    CompanyProfile mapToCompanyProfile(CompanyProfileApi companyProfileApi);
}
