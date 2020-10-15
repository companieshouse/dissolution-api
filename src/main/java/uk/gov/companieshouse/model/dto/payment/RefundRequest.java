package uk.gov.companieshouse.model.dto.payment;

import javax.validation.constraints.NotBlank;

public class RefundRequest {

    @NotBlank
    private int amount;

    public RefundRequest(int amount) {
        this.setAmount(amount);
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
