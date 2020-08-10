package uk.gov.companieshouse.model.dto.chips;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChipsFormAttachment {

    @JsonProperty("mimetype")
    private String mimeType;

    private String category;

    private String data;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
