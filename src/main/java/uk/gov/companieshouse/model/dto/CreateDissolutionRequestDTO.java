package uk.gov.companieshouse.model.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class CreateDissolutionRequestDTO {

    @Size(min = 1, message = "At least 1 director must be provided")
    @Valid
    private List<DirectorRequestDTO> directors;

    public List<DirectorRequestDTO> getDirectors() {
        return directors;
    }

    public void setDirectors(List<DirectorRequestDTO> directors) {
        this.directors = directors;
    }
}
