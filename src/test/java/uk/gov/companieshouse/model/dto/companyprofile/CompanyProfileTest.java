package uk.gov.companieshouse.model.dto.companyprofile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CompanyProfileTest {

    @Test
    void builder_createsCompanyProfileWithAllFields() {
        CompanyProfile profile = new CompanyProfile.Builder()
                .withCompanyName("Test Ltd")
                .withType("ltd")
                .withCompanyNumber("12345")
                .withCompanyStatus("active")
                .build();
        assertEquals("Test Ltd", profile.getCompanyName());
        assertEquals("ltd", profile.getType());
        assertEquals("12345", profile.getCompanyNumber());
        assertEquals("active", profile.getCompanyStatus());
    }

    @Test
    void setters_updateFieldsCorrectly() {
        CompanyProfile profile = new CompanyProfile.Builder().build();
        profile.setCompanyName("New Name");
        profile.setType("plc");
        profile.setCompanyNumber("99999");
        profile.setCompanyStatus("dissolved");
        assertEquals("New Name", profile.getCompanyName());
        assertEquals("plc", profile.getType());
        assertEquals("99999", profile.getCompanyNumber());
        assertEquals("dissolved", profile.getCompanyStatus());
    }

    @Test
    void equals_returnsTrueForIdenticalObjects() {
        CompanyProfile a = new CompanyProfile.Builder()
                .withCompanyName("A")
                .withType("ltd")
                .withCompanyNumber("1")
                .withCompanyStatus("active")
                .build();
        CompanyProfile b = new CompanyProfile.Builder()
                .withCompanyName("A")
                .withType("ltd")
                .withCompanyNumber("1")
                .withCompanyStatus("active")
                .build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_returnsFalseForDifferentObjects() {
        CompanyProfile a = new CompanyProfile.Builder()
                .withCompanyName("A")
                .withType("ltd")
                .withCompanyNumber("1")
                .withCompanyStatus("active")
                .build();
        CompanyProfile b = new CompanyProfile.Builder()
                .withCompanyName("B")
                .withType("plc")
                .withCompanyNumber("2")
                .withCompanyStatus("dissolved")
                .build();
        assertNotEquals(a, b);
    }

    @Test
    void equals_returnsFalseWhenComparedWithNullOrDifferentType() {
        CompanyProfile profile = new CompanyProfile.Builder().build();
        assertNotEquals(null, profile);
        assertNotEquals("not a CompanyProfile", profile);
    }

    @Test
    void hashCode_isConsistentForSameObject() {
        CompanyProfile profile = new CompanyProfile.Builder()
                .withCompanyName("A")
                .withType("ltd")
                .withCompanyNumber("1")
                .withCompanyStatus("active")
                .build();
        int hash1 = profile.hashCode();
        int hash2 = profile.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    void builder_allowsNullFields() {
        CompanyProfile profile = new CompanyProfile.Builder()
                .withCompanyName(null)
                .withType(null)
                .withCompanyNumber(null)
                .withCompanyStatus(null)
                .build();
        assertNull(profile.getCompanyName());
        assertNull(profile.getType());
        assertNull(profile.getCompanyNumber());
        assertNull(profile.getCompanyStatus());
    }
}

