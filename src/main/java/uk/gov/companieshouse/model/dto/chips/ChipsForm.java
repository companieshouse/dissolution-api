package uk.gov.companieshouse.model.dto.chips;

import java.util.List;

public class ChipsForm {

    private String barcode;
    private String xml;
    private List<ChipsFormAttachment> attachments;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public List<ChipsFormAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ChipsFormAttachment> attachments) {
        this.attachments = attachments;
    }
}
