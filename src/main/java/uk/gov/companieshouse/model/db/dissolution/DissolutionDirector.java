package uk.gov.companieshouse.model.db.dissolution;

import org.springframework.data.mongodb.core.mapping.Field;

public class DissolutionDirector {

    @Field("officer_id")
    private String officerId;

    private String name;

    private String email;

    @Field("on_behalf_name")
    private String onBehalfName;

    private DirectorApproval approval;

    public String getOfficerId() {
        return officerId;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOnBehalfName() {
        return onBehalfName;
    }

    public void setOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
    }

    public boolean hasDirectorApproval() {
        return approval != null;
    }

    public DirectorApproval getDirectorApproval() {
        return approval;
    }

    public void setDirectorApproval(DirectorApproval approval) {
        this.approval = approval;
    }
}
