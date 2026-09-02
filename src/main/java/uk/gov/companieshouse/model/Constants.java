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
   public static final String PAYMENT_DESCRIPTION = "Apply to strike off and dissolve a company: %s (%s)";
   public static final String PAYMENT_DESCRIPTION_IDENTIFIER = "Dissolution application";
   public static final String PAYMENT_AVAILABLE_PAYMENT_METHOD = "credit-card";
   public static final String PAYMENT_CLASS_OF_PAYMENT = "data-maintenance";

   /* Email */
   public static final String EMAIL_APP_ID = "dissolution-api";
   public static final String EMAIL_TOPIC = "email-send";

   public static final String SUCCESSFUL_PAYMENT_EMAIL_SUBJECT = "Your application to strike off and dissolve a company has been submitted";
   public static final String APPLICATION_ACCEPTED_EMAIL_SUBJECT = "Strike off and dissolve a company - application accepted";
   public static final String APPLICATION_REJECTED_EMAIL_SUBJECT = "Your application to strike off and dissolve a company has been rejected";
   public static final String SIGNATORY_TO_SIGN_EMAIL_SUBJECT = "You need to sign the application to strike off and dissolve a company";
   public static final String PENDING_PAYMENT_EMAIL_SUBJECT = "Pay for your application to strike off and dissolve a company";
   public static final String DISSOLUTION_SUBMISSION_ALERT = "eDS01 Submission Alert";

   /* Headers */
   public static final String HEADER_AUTHORIZATION = "Authorization";
   public static final String HEADER_ACCEPT = "Accept";
   public static final String HEADER_CONTENT_TYPE = "Content-Type";
   public static final String HEADER_ERIC_REQUEST_ID = "X-Request-Id";

   public static final String CONTENT_TYPE_HTML = "text/html";
   public static final String CONTENT_TYPE_JSON = "application/json";
   public static final String CONTENT_TYPE_PDF = "application/pdf";

   /* Path attributes */
   public static final String TRANSACTION_ID_KEY = "transaction_id";
   public static final String DISSOLUTION_ID_KEY = "dissolution_id";

   // COMPANY_NUMBER_KEY has to use kebab-case to remain backward compatible with
   // the pre-migration dissolution controller and DissolutionTokenPermissionsInterceptor
   public static final String COMPANY_NUMBER_KEY = "company-number";

   /* Request attributes */
   public static final String TRANSACTION_KEY = "transaction";
   public static final String OFFICER_ID_KEY = "officer_id";

   /* Filings */
   public static final String FILING_KIND_DS01 = "dissolution#ds01";
   public static final String FILING_KIND_LLDS01 = "dissolution#llds01";
   public static final String FILING_TYPE_PREFIX_DISSOLUTION = "dissolution";

   /* Transactions */
   public static final String SUBMISSION_URI_PATTERN = "/transactions/%s/dissolution/%s";
   public static final String LINK_RESOURCE = "resource";

   /* Certificate */
   public static final String CERTIFICATE_FILE_NAME_PREFIX = "Apply-to-strike-off-and-dissolve-a-company";
   public static final String S3_URI_PATTERN = "s3://%s/%s/dissolution/%s-%s.pdf";
}
