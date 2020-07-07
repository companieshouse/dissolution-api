package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.DirectorApproval;
import uk.gov.companieshouse.model.db.DissolutionDirector;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DirectorApprovalMapper {
    public DirectorApproval mapToDirectorApproval(String userId, String ip) {
        final DirectorApproval approval = new DirectorApproval();
        approval.setUserId(userId);
        approval.setIpAddress(ip);
        approval.setDateTime(LocalDateTime.now());
        return approval;
    }
}
