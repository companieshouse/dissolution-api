package uk.gov.companieshouse.model.dto.documentrender;

import java.util.List;

public class DissolutionCertificateData {

    private String cdn;
    private String companyNumber;
    private String companyName;
    private List<DissolutionCertificateDirector> directors;

    public String getCdn() {
        return cdn;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }

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
