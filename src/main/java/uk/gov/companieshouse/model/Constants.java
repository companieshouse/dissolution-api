package uk.gov.companieshouse.model;

public final class Constants {

   private Constants() {
   }

   /* Dissolution */
   public static final String DISSOLUTION_KIND = "dissolution-request#dissolution-request";

   /* Payment */
   public static final String PAYMENT_KIND = "dissolution-request#payment";
   public static final String PAYMENT_ITEM_KIND = "dissolution-request#payment-details";
   public static final String PAYMENT_RESOURCE_KIND = "dissolution-request#dissolution-request";
   public static final String PAYMENT_DESCRIPTION = "Dissolution application";
   public static final String PAYMENT_DESCRIPTION_IDENTIFIER = "Dissolution application";
   public static final String PAYMENT_PRODUCT_TYPE = "Dissolution application";
   public static final String PAYMENT_AMOUNT = "8";
   public static final String PAYMENT_AVAILABLE_PAYMENT_METHOD = "credit-card";
   public static final String PAYMENT_CLASS_OF_PAYMENT = "data-maintenance";

   /* CHIPS */
   public static final int CHIPS_RETRY_DELAY_MINUTES = 30;
   public static final int CHIPS_SUBMISSION_LIMIT = 2;


}
