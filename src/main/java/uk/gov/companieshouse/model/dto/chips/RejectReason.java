package uk.gov.companieshouse.model.dto.chips;

public class RejectReason {

    private String id;

    private String description;

    private String textEnglish;

    private String textWelsh;

    private int order;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTextEnglish() {
        return textEnglish;
    }

    public void setTextEnglish(String textEnglish) {
        this.textEnglish = textEnglish;
    }

    public String getTextWelsh() {
        return textWelsh;
    }

    public void setTextWelsh(String textWelsh) {
        this.textWelsh = textWelsh;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
