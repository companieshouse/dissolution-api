package uk.gov.companieshouse.model.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class DissolutionCreateRequest {

    @Size(min = 1, message = "At least 1 director must be provided")
    @Valid
    private List<DirectorRequest> directors;

    public List<DirectorRequest> getDirectors() {
        return directors;
    }

    public void setDirectors(List<DirectorRequest> directors) {
        this.directors = directors;
    }
}
