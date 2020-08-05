package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DirectorApprovalMapperTest {
    private static final String USER_ID = "user123";
    private static final String IP_ADDRESS = "192.168.0.1";

    private final DirectorApprovalMapper mapper = new DirectorApprovalMapper();

    @Test
    public void mapToDirectorApproval_getsDirectorApproval() {
        final DirectorApproval approval = mapper.mapToDirectorApproval(USER_ID, IP_ADDRESS);

        assertNotNull(approval.getDateTime());
        assertEquals(USER_ID, approval.getUserId());
        assertEquals(IP_ADDRESS, approval.getIpAddress());
    }
}
