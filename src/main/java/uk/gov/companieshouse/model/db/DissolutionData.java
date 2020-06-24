package uk.gov.companieshouse.model.db;

import java.util.List;

public class DissolutionData {

    private String eTag;
    private DissolutionApplication application;
    private List<DissolutionDirector> directors;

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public DissolutionApplication getApplication() {
        return application;
    }

    public void setApplication(DissolutionApplication application) {
        this.application = application;
    }

    public List<DissolutionDirector> getDirectors() {
        return directors;
    }

    public void setDirectors(List<DissolutionDirector> directors) {
        this.directors = directors;
    }
}
