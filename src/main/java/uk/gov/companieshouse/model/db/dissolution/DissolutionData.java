package uk.gov.companieshouse.model.db.dissolution;

import java.util.List;

public class DissolutionData {

    private String etag;

    private DissolutionApplication application;

    private List<DissolutionDirector> directors;

    public String getETag() {
        return etag;
    }

    public void setETag(String eTag) {
        this.etag = eTag;
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
