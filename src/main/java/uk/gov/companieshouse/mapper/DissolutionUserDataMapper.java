package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.DissolutionUserData;

@Service
public class DissolutionUserDataMapper {
    public DissolutionUserData mapToUserData(String userId, String ip, String email) {
        final DissolutionUserData userData = new DissolutionUserData();
        userData.setUserId(userId);
        userData.setIpAddress(ip);
        userData.setEmail(email);
        return userData;
    }
}
