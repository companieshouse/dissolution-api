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

   /* Email */
   public static final String EMAIL_APP_ID = "dissolution-api";
   public static final String EMAIL_TOPIC = "email-send";
   public static final String SUCCESSFUL_PAYMENT_EMAIL_SUBJECT = "Your application has been submitted";
   public static final String SUCCESSFUL_PAYMENT_MESSAGE_TYPE = "dissolution-payment-confirmation";
}
