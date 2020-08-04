package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.enums.OfficerRole;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;

public class CompanyOfficerValidatorTest {
    private static final String DIRECTOR_NAME = "Jeff";
    private static final String DIRECTOR_EMAIL = "jeff@email.com";
    private static final String DIRECTOR_NAME_TWO = "Bill";
    private static final String DIRECTOR_EMAIL_TWO = "bill@email.com";
    private static final String DIRECTOR_NAME_THREE = "Ted";

    private final CompanyOfficerValidator validator = new CompanyOfficerValidator();

    @Test
    public void areMajorityOfCompanyOfficersSelected_halfDirectorsSelected_returnsTrue() {
        final DirectorRequest directorOne = new DirectorRequest();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);

        final DirectorRequest directorTwo = new DirectorRequest();
        directorTwo.setName(DIRECTOR_NAME_TWO);
        directorTwo.setEmail(DIRECTOR_EMAIL_TWO);

        final List<DirectorRequest> selectedDirectors = Arrays.asList(directorOne, directorTwo);

        final CompanyOfficer officerOne = new CompanyOfficer();
        officerOne.setName(DIRECTOR_NAME);
        officerOne.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerOne.setResignedOn(null);

        final CompanyOfficer officerTwo = new CompanyOfficer();
        officerTwo.setName(DIRECTOR_NAME_TWO);
        officerTwo.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerTwo.setResignedOn(null);

        final CompanyOfficer officerThree = new CompanyOfficer();
        officerThree.setName(DIRECTOR_NAME_THREE);
        officerThree.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerThree.setResignedOn(null);

        final List<CompanyOfficer> companyOfficers = Arrays.asList(
                officerOne,
                officerTwo,
                officerThree
        );

        final boolean hasOverHalfDirectorsSelected =
                validator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertTrue(hasOverHalfDirectorsSelected);
    }

    @Test
    public void areMajorityOfCompanyOfficersSelected_allDirectorsSelected_returnsTrue() {
        final DirectorRequest directorOne = new DirectorRequest();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);

        final DirectorRequest directorTwo = new DirectorRequest();
        directorTwo.setName(DIRECTOR_NAME_TWO);
        directorTwo.setEmail(DIRECTOR_EMAIL_TWO);

        final List<DirectorRequest> selectedDirectors = Arrays.asList(directorOne, directorTwo);

        final CompanyOfficer officerOne = new CompanyOfficer();
        officerOne.setName(DIRECTOR_NAME);
        officerOne.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerOne.setResignedOn(null);

        final CompanyOfficer officerTwo = new CompanyOfficer();
        officerTwo.setName(DIRECTOR_NAME_TWO);
        officerTwo.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerTwo.setResignedOn(null);

        final List<CompanyOfficer> companyOfficers = Arrays.asList(
                officerOne,
                officerTwo
        );

        final boolean hasOverHalfDirectorsSelected =
                validator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertTrue(hasOverHalfDirectorsSelected);
    }

    @Test
    public void areMajorityOfCompanyOfficersSelected_noDirectorsSelected_returnsFalse() {
        final List<DirectorRequest> selectedDirectors = Collections.emptyList();
        final List<CompanyOfficer> companyOfficers = Collections.singletonList(generateCompanyOfficer());

        final boolean hasOverHalfDirectorsSelected =
                validator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertFalse(hasOverHalfDirectorsSelected);
    }

    @Test
    public void areMajorityOfCompanyOfficersSelected_wrongDirectorsSelected_returnsFalse() {
        final DirectorRequest directorOne = new DirectorRequest();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);

        final DirectorRequest directorTwo = new DirectorRequest();
        directorTwo.setName(DIRECTOR_NAME_TWO);
        directorTwo.setEmail(DIRECTOR_EMAIL_TWO);
        final List<DirectorRequest> selectedDirectors = Arrays.asList(directorOne, directorTwo);

        final CompanyOfficer officerOne = new CompanyOfficer();
        officerOne.setName("Other officer");
        officerOne.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerOne.setResignedOn(null);

        final CompanyOfficer officerTwo = new CompanyOfficer();
        officerTwo.setName("Not the same officer");
        officerTwo.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerTwo.setResignedOn(null);

        final List<CompanyOfficer> companyOfficers = Arrays.asList(
                officerOne,
                officerTwo
        );

        final boolean hasOverHalfDirectorsSelected =
                validator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertFalse(hasOverHalfDirectorsSelected);
    }

    @Test
    public void areMajorityOfCompanyOfficersSelected_lessThanHalfDirectorsSelected_returnsFalse() {
        final DirectorRequest directorOne = new DirectorRequest();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);

        final List<DirectorRequest> selectedDirectors = Arrays.asList(directorOne);

        final CompanyOfficer officerOne = new CompanyOfficer();
        officerOne.setName(DIRECTOR_NAME);
        officerOne.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerOne.setResignedOn(null);

        final CompanyOfficer officerTwo = new CompanyOfficer();
        officerTwo.setName(DIRECTOR_NAME_TWO);
        officerTwo.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerTwo.setResignedOn(null);

        final CompanyOfficer officerThree = new CompanyOfficer();
        officerThree.setName(DIRECTOR_NAME_THREE);
        officerThree.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officerThree.setResignedOn(null);

        final List<CompanyOfficer> companyOfficers = Arrays.asList(
                officerOne,
                officerTwo,
                officerThree
        );

        final boolean hasOverHalfDirectorsSelected =
                validator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertFalse(hasOverHalfDirectorsSelected);
    }
}
