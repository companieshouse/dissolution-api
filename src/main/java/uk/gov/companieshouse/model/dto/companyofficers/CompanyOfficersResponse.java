package uk.gov.companieshouse.model.dto.companyofficers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyOfficersResponse {

    private List<CompanyOfficer> items;

    public List<CompanyOfficer> getItems() {
        return items;
    }

    public void setItems(List<CompanyOfficer> items) {
        this.items = items;
    }
}
