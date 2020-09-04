package uk.gov.companieshouse.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.util.List;

public class PaymentItem {
    private String description;

    @JsonProperty("description_identifier")
    private String descriptionIdentifier;

    @JsonProperty("description_values")
    private PaymentDescriptionValues descriptionValues;

    @JsonProperty("product_type")
    private ApplicationType productType;

    private String amount;

    @JsonProperty("available_payment_methods")
    private List<String> availablePaymentMethods;

    @JsonProperty("class_of_payment")
    private List<String> classOfPayment;

    private String kind;

    @JsonProperty("resource_kind")
    private String resourceKind;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionIdentifier() {
        return descriptionIdentifier;
    }

    public void setDescriptionIdentifier(String descriptionIdentifier) {
        this.descriptionIdentifier = descriptionIdentifier;
    }

    public PaymentDescriptionValues getDescriptionValues() {
        return descriptionValues;
    }

    public void setDescriptionValues(PaymentDescriptionValues descriptionValues) {
        this.descriptionValues = descriptionValues;
    }

    public ApplicationType getProductType() {
        return productType;
    }

    public void setProductType(ApplicationType productType) {
        this.productType = productType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public List<String> getAvailablePaymentMethods() {
        return availablePaymentMethods;
    }

    public void setAvailablePaymentMethods(List<String> availablePaymentMethods) {
        this.availablePaymentMethods = availablePaymentMethods;
    }

    public List<String> getClassOfPayment() {
        return classOfPayment;
    }

    public void setClassOfPayment(List<String> classOfPayment) {
        this.classOfPayment = classOfPayment;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getResourceKind() {
        return resourceKind;
    }

    public void setResourceKind(String resourceKind) {
        this.resourceKind = resourceKind;
    }
}
