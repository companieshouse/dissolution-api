package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.officers.OfficerRoleApi;
import uk.gov.companieshouse.fixtures.CompanyOfficerFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.service.CompanyOfficerValidator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CompanyOfficerValidatorTest {
    private static final String DIRECTOR_NAME = "Jeff";
    private static final String DIRECTOR_EMAIL = "jeff@email.com";
    private static final String DIRECTOR_NAME_TWO = "Bill";
    private static final String DIRECTOR_EMAIL_TWO = "bill@email.com";
    private static final String DIRECTOR_NAME_THREE = "Ted";

    private final CompanyOfficerValidator mapper = new CompanyOfficerValidator();

    @Test
    public void mapCompanyOfficersToIsOverHalfSelected_halfDirectorsSelected_returnsTrue() {
        final DissolutionDirector directorOne = new DissolutionDirector();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);
        final DissolutionDirector directorTwo = new DissolutionDirector();
        directorTwo.setName(DIRECTOR_NAME_TWO);
        directorTwo.setEmail(DIRECTOR_EMAIL_TWO);
        final List<DissolutionDirector> selectedDirectors = Arrays.asList(directorOne, directorTwo);

        final CompanyOfficerApi companyOfficerApiOne = new CompanyOfficerApi();
        companyOfficerApiOne.setName(DIRECTOR_NAME);
        companyOfficerApiOne.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiOne.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final CompanyOfficerApi companyOfficerApiTwo = new CompanyOfficerApi();
        companyOfficerApiTwo.setName(DIRECTOR_NAME_TWO);
        companyOfficerApiTwo.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiTwo.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final CompanyOfficerApi companyOfficerApiThree = new CompanyOfficerApi();
        companyOfficerApiThree.setName(DIRECTOR_NAME_THREE);
        companyOfficerApiThree.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiThree.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final List<CompanyOfficerApi> companyOfficers = Arrays.asList(
                companyOfficerApiOne,
                companyOfficerApiTwo,
                companyOfficerApiThree
        );

        final boolean hasOverHalfDirectorsSelected =
                mapper.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertTrue(hasOverHalfDirectorsSelected);
    }

    @Test
    public void mapCompanyOfficersToIsOverHalfSelected_allDirectorsSelected_returnsTrue() {
        final DissolutionDirector directorOne = new DissolutionDirector();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);
        final DissolutionDirector directorTwo = new DissolutionDirector();
        directorTwo.setName(DIRECTOR_NAME_TWO);
        directorTwo.setEmail(DIRECTOR_EMAIL_TWO);
        final List<DissolutionDirector> selectedDirectors = Arrays.asList(directorOne, directorTwo);

        final CompanyOfficerApi companyOfficerApiOne = new CompanyOfficerApi();
        companyOfficerApiOne.setName(DIRECTOR_NAME);
        companyOfficerApiOne.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiOne.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final CompanyOfficerApi companyOfficerApiTwo = new CompanyOfficerApi();
        companyOfficerApiTwo.setName(DIRECTOR_NAME_TWO);
        companyOfficerApiTwo.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiTwo.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final List<CompanyOfficerApi> companyOfficers = Arrays.asList(
                companyOfficerApiOne,
                companyOfficerApiTwo
        );

        final boolean hasOverHalfDirectorsSelected =
                mapper.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertTrue(hasOverHalfDirectorsSelected);
    }

    @Test
    public void mapCompanyOfficersToIsOverHalfSelected_noDirectorsSelected_returnsFalse() {
        final List<DissolutionDirector> selectedDirectors = Collections.emptyList();
        final List<CompanyOfficerApi> companyOfficers = CompanyOfficerFixtures.generateCompanyOfficerList();

        final boolean hasOverHalfDirectorsSelected =
                mapper.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertFalse(hasOverHalfDirectorsSelected);
    }

    @Test
    public void mapCompanyOfficersToIsOverHalfSelected_wrongDirectorsSelected_returnsFalse() {
        final DissolutionDirector directorOne = new DissolutionDirector();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);
        final DissolutionDirector directorTwo = new DissolutionDirector();
        directorTwo.setName(DIRECTOR_NAME_TWO);
        directorTwo.setEmail(DIRECTOR_EMAIL_TWO);
        final List<DissolutionDirector> selectedDirectors = Arrays.asList(directorOne, directorTwo);

        final CompanyOfficerApi companyOfficerApiOne = new CompanyOfficerApi();
        companyOfficerApiOne.setName("Other officer");
        companyOfficerApiOne.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiOne.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final CompanyOfficerApi companyOfficerApiTwo = new CompanyOfficerApi();
        companyOfficerApiTwo.setName("Not the same officer");
        companyOfficerApiTwo.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiTwo.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final List<CompanyOfficerApi> companyOfficers = Arrays.asList(
                companyOfficerApiOne,
                companyOfficerApiTwo
        );

        final boolean hasOverHalfDirectorsSelected =
                mapper.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertFalse(hasOverHalfDirectorsSelected);
    }

    @Test
    public void mapCompanyOfficersToIsOverHalfSelected_lessThanHalfDirectorsSelected_returnsFalse() {
        final DissolutionDirector directorOne = new DissolutionDirector();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);
        final List<DissolutionDirector> selectedDirectors = Arrays.asList(directorOne);

        final CompanyOfficerApi companyOfficerApiOne = new CompanyOfficerApi();
        companyOfficerApiOne.setName(DIRECTOR_NAME);
        companyOfficerApiOne.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiOne.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final CompanyOfficerApi companyOfficerApiTwo = new CompanyOfficerApi();
        companyOfficerApiTwo.setName(DIRECTOR_NAME_TWO);
        companyOfficerApiTwo.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiTwo.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final CompanyOfficerApi companyOfficerApiThree = new CompanyOfficerApi();
        companyOfficerApiThree.setName(DIRECTOR_NAME_THREE);
        companyOfficerApiThree.setOfficerRole(OfficerRoleApi.DIRECTOR);
        companyOfficerApiThree.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());
        final List<CompanyOfficerApi> companyOfficers = Arrays.asList(
                companyOfficerApiOne,
                companyOfficerApiTwo,
                companyOfficerApiThree
        );


        final boolean hasOverHalfDirectorsSelected =
                mapper.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);

        assertFalse(hasOverHalfDirectorsSelected);
    }
}
