package uk.gov.companieshouse.model.dto.documentRender;

import java.util.List;

public class DissolutionCertificateData {

    private String companyNumber;
    private String companyName;
    private List<DissolutionCertificateDirector> directors;

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<DissolutionCertificateDirector> getDirectors() {
        return directors;
    }

    public void setDirectors(List<DissolutionCertificateDirector> directors) {
        this.directors = directors;
    }
}
