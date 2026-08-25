package uk.gov.companieshouse.model.dto.dissolution;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class DissolutionInitiationRequest {

    @NotEmpty(message = "At least 1 director must be provided")
    private List<@NotNull @Valid DirectorRequest> directors;

    public List<DirectorRequest> getDirectors() {
        return directors;
    }

    public void setDirectors(List<DirectorRequest> directors) {
        this.directors = directors;
    }
}
