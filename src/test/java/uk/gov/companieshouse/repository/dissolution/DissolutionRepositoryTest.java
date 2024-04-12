package uk.gov.companieshouse.repository.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.enums.SubmissionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class DissolutionRepositoryTest {

    @Autowired
    public DissolutionRepository repository;

    @Test
    void findByCompanyNumber_findsActiveDissolution() {
        final String COMPANY_NUMBER = "711"; //This is a random number. Reference should be unique everytime you test

        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getCompany().setNumber(COMPANY_NUMBER);
        dissolution.setActive(true);

        repository.insert(dissolution);

        assertEquals(COMPANY_NUMBER, repository.findByCompanyNumber(COMPANY_NUMBER).get().getCompany().getNumber());
    }

    @Test
    void findByCompanyNumber_DoesNotFindInactiveDissolution() {
        final String COMPANY_NUMBER = "211"; //This is a random number. Reference should be unique everytime you test

        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getCompany().setNumber(COMPANY_NUMBER);
        dissolution.setActive(false);

        repository.insert(dissolution);

        assertTrue(repository.findByCompanyNumber(COMPANY_NUMBER).isEmpty());
    }

    @Test
    void findByDataApplicationReference_findsCorrectDissolution() {
        final String APPLICATION_REFERENCE = "GQB911"; //This is a random string. Reference should be unique everytime you test

        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getData().getApplication().setReference(APPLICATION_REFERENCE);

        repository.insert(dissolution);

        assertEquals(APPLICATION_REFERENCE, repository.findByDataApplicationReference(APPLICATION_REFERENCE).get().getData().getApplication().getReference());
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

        repository.insert(dissolution1); // Not eligible - wrong status
        repository.insert(dissolution2); // Eligible - correct status and date time is null
        repository.insert(dissolution3); // Eligible - correct status and date time is correct
        repository.insert(dissolution4); // Not eligible - wrong date time
        repository.insert(dissolution5); // Eligible - correct status and date time is correct, but over the fetch limit

        ArrayList<Dissolution> dissolutions = new ArrayList<>(repository.findPendingDissolutions(
                LocalDateTime.now().minusMinutes(30),
                PageRequest.of(0, SUBMISSION_LIMIT, Sort.Direction.ASC, "payment.date_time")
        ));

        assertEquals(SUBMISSION_LIMIT, dissolutions.size());

        // Order is important - older first
        assertEquals("3", dissolutions.get(0).getCompany().getNumber());
        assertEquals("3", dissolutions.get(1).getCompany().getNumber());
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
