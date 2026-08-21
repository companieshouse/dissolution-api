package uk.gov.companieshouse.repository.dissolution;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.config.AbstractMongoConfig;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.enums.SubmissionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.model.enums.DissolutionStatus.DRAFT;
import static uk.gov.companieshouse.model.enums.DissolutionStatus.SUBMITTED;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class DissolutionRepositoryTest extends AbstractMongoConfig {

    @BeforeAll
    static void setup() {
        mongoDBContainer.start();
    }

    @Autowired
    public DissolutionRepository dissolutionRepository;

    @Test
    void findByCompanyNumber_findsActiveDissolution() {
        final String COMPANY_NUMBER = "711"; //This is a random number. Reference should be unique everytime you test

        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getCompany().setNumber(COMPANY_NUMBER);
        dissolution.setActive(true);

        dissolutionRepository.insert(dissolution);

        assertEquals(COMPANY_NUMBER, dissolutionRepository.findByCompanyNumber(COMPANY_NUMBER).get().getCompany().getNumber());
    }

    @Test
    void findByCompanyNumber_DoesNotFindInactiveDissolution() {
        final String COMPANY_NUMBER = "211"; //This is a random number. Reference should be unique everytime you test

        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getCompany().setNumber(COMPANY_NUMBER);
        dissolution.setActive(false);

        dissolutionRepository.insert(dissolution);

        assertTrue(dissolutionRepository.findByCompanyNumber(COMPANY_NUMBER).isEmpty());
    }

    @Test
    void findByDataApplicationReference_findsCorrectDissolution() {
        final String APPLICATION_REFERENCE = "GQB911"; //This is a random string. Reference should be unique everytime you test

        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getData().getApplication().setReference(APPLICATION_REFERENCE);

        dissolutionRepository.insert(dissolution);

        assertEquals(APPLICATION_REFERENCE, dissolutionRepository.findByDataApplicationReference(APPLICATION_REFERENCE).get().getData().getApplication().getReference());
    }

    @Test
    void findPendingDissolutions_findsDissolutionsThatArePendingAndWithCorrectSubmissionDateTime() {
        final int SUBMISSION_LIMIT = 2;

        Dissolution dissolution1 = this.generateDissolution("1", LocalDateTime.now().minusMinutes(50),
                SubmissionStatus.FAILED, LocalDateTime.now().minusMinutes(240));
        Dissolution dissolution2 = this.generateDissolution("2", null,
                SubmissionStatus.PENDING, LocalDateTime.now().minusMinutes(180));
        Dissolution dissolution3 = this.generateDissolution("3", LocalDateTime.now().minusMinutes(45),
                SubmissionStatus.PENDING, LocalDateTime.now().minusMinutes(200));
        Dissolution dissolution4 = this.generateDissolution("4", LocalDateTime.now().minusMinutes(20),
                SubmissionStatus.PENDING, LocalDateTime.now().minusMinutes(180));
        Dissolution dissolution5 = this.generateDissolution("5", LocalDateTime.now().minusMinutes(55),
                SubmissionStatus.PENDING, LocalDateTime.now().minusMinutes(160));

        dissolutionRepository.insert(dissolution1); // Not eligible - wrong status
        dissolutionRepository.insert(dissolution2); // Eligible - correct status and date time is null
        dissolutionRepository.insert(dissolution3); // Eligible - correct status and date time is correct
        dissolutionRepository.insert(dissolution4); // Not eligible - wrong date time
        dissolutionRepository.insert(dissolution5); // Eligible - correct status and date time is correct, but over the fetch limit

        ArrayList<Dissolution> dissolutions = new ArrayList<>(dissolutionRepository.findPendingDissolutions(
                LocalDateTime.now().minusMinutes(30),
                PageRequest.of(0, SUBMISSION_LIMIT, Sort.Direction.ASC, "payment.date_time")
        ));

        assertEquals(SUBMISSION_LIMIT, dissolutions.size());

        // Order is important - older first
        assertEquals("3", dissolutions.get(0).getCompany().getNumber());
        assertEquals("2", dissolutions.get(1).getCompany().getNumber());
    }

    @Nested
    @DisplayName("findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc")
    class FindFirstByCompanyNumberAndStatus {

        @Test
        void findsMostRecentlySubmittedDissolution() {
            final String COMPANY_NUMBER = "913";

            Dissolution older = aDissolution()
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withStatus(SUBMITTED, LocalDateTime.of(2024, 1, 1, 12, 0))
                    .build();

            Dissolution newer = aDissolution()
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withStatus(SUBMITTED, LocalDateTime.of(2024, 1, 1, 12, 5))
                    .build();

            Dissolution draft = aDissolution()
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withStatus(DRAFT)
                    .build();

            dissolutionRepository.insert(newer);
            dissolutionRepository.insert(older);
            dissolutionRepository.insert(draft);

            final Dissolution result = dissolutionRepository
                    .findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)
                    .orElseThrow();

            assertEquals(newer.getId(), result.getId());
            assertEquals(SUBMITTED, result.getStatus());
        }

        @Test
        void whenNoSubmittedDissolutionThenEmptyReturned() {
            final String COMPANY_NUMBER = "914";
            final String OTHER_COMPANY_NUMBER = "915";

            Dissolution draft = aDissolution()
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withStatus(DRAFT)
                    .build();

            Dissolution otherCompanySubmitted = aDissolution()
                    .withCompanyNumber(OTHER_COMPANY_NUMBER)
                    .withStatus(SUBMITTED, LocalDateTime.of(2024, 1, 1, 12, 0))
                    .build();

            dissolutionRepository.insert(draft);
            dissolutionRepository.insert(otherCompanySubmitted);

            assertTrue(dissolutionRepository
                    .findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)
                    .isEmpty());
        }
    }

    private Dissolution generateDissolution(String companyNumber, LocalDateTime submissionDateTime, SubmissionStatus submissionStatus, LocalDateTime paymentDateTime) {
        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getCompany().setNumber(companyNumber);
        dissolution.getSubmission().setDateTime(submissionDateTime);
        dissolution.getSubmission().setStatus(submissionStatus);
        dissolution.getPaymentInformation().setDateTime(paymentDateTime);

        return dissolution;
    }
}
