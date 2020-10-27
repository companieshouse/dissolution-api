package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;

import static uk.gov.companieshouse.util.DateTimeGenerator.generateCurrentDateTime;

@Service
public class DirectorApprovalMapper {
    public DirectorApproval mapToDirectorApproval(String userId, String ip) {
        final DirectorApproval approval = new DirectorApproval();
        approval.setUserId(userId);
        approval.setIpAddress(ip);
        approval.setDateTime(generateCurrentDateTime());
        return approval;
    }
}
