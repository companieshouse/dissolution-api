package uk.gov.companieshouse.model.dto.dissolution;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

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
