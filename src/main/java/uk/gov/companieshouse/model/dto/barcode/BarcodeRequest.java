package uk.gov.companieshouse.model.dto.barcode;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BarcodeRequest {

    @JsonProperty("datereceived")
    private int dateReceived;

    public int getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(int dateReceived) {
        this.dateReceived = dateReceived;
    }
}
