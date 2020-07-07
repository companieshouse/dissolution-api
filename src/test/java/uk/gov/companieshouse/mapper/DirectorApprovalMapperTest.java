package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.DirectorApproval;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.db.DissolutionDirector;

import java.util.List;

import static org.junit.Assert.*;

public class DirectorApprovalMapperTest {
    private static final String USER_ID = "user123";
    private static final String IP_ADDRESS = "192.168.0.1";
    private static final String EMAIL = "user@mail.com";

    private final DirectorApprovalMapper mapper = new DirectorApprovalMapper();

    @Test
    public void mapToDirectorApproval_getsDirectorApproval() {
        final DirectorApproval approval = mapper.mapToDirectorApproval(USER_ID, IP_ADDRESS);

        assertNotNull(approval.getDateTime());
        assertEquals(USER_ID, approval.getUserId());
        assertEquals(IP_ADDRESS, approval.getIpAddress());
    }
}
