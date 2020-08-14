package uk.gov.companieshouse.model.dto.chips;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DissolutionChipsRequest {

    @JsonProperty("packagemetadata")
    private ChipsPackageMetadata packageMetadata;

    private List<ChipsForm> forms;

    public ChipsPackageMetadata getPackageMetadata() {
        return packageMetadata;
    }

    public void setPackageMetadata(ChipsPackageMetadata packageMetadata) {
        this.packageMetadata = packageMetadata;
    }

    public List<ChipsForm> getForms() {
        return forms;
    }

    public void setForms(List<ChipsForm> forms) {
        this.forms = forms;
    }
}
