package uk.gov.companieshouse.model.dto.payment;

public class RefundRequest {

    private int amount;

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
