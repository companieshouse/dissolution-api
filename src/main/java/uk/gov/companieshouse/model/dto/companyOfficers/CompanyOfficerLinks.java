package uk.gov.companieshouse.model.dto.companyOfficers;

public class CompanyOfficerLinks {

    public static class OfficerLinks {

        private String appointments;

        public String getAppointments() {
            return appointments;
        }

        public void setAppointments(String appointments) {
            this.appointments = appointments;
        }
    }

    private OfficerLinks officer;

    public OfficerLinks getOfficer() {
        return officer;
    }

    public void setOfficer(OfficerLinks officer) {
        this.officer = officer;
    }
}
