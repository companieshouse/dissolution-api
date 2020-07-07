package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DirectorApprovalMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.DirectorApproval;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.db.DissolutionDirector;
import uk.gov.companieshouse.model.dto.DissolutionPatchResponse;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DissolutionPatcher {

    private final DissolutionRepository repository;
    private final DissolutionResponseMapper responseMapper;
    private final DirectorApprovalMapper approvalMapper;

    @Autowired
    public DissolutionPatcher(
        DissolutionRepository repository,
        DissolutionResponseMapper responseMapper,
        DirectorApprovalMapper approvalMapper) {
        this.repository = repository;
        this.responseMapper = responseMapper;
        this.approvalMapper = approvalMapper;
    }

    public DissolutionPatchResponse patch(String companyNumber, String userId, String ip, String email) {
        final Dissolution dissolution = this.repository.findByCompanyNumber(companyNumber).get();

        this.addDirectorApproval(userId, ip, email, dissolution);

        if (!this.hasDirectorsLeftToApprove(dissolution)) {
            this.setDissolutionToPendingPayment(dissolution);
        }

        this.repository.save(dissolution);

        return this.responseMapper.mapToDissolutionPatchResponse(companyNumber);
    }

    private DissolutionDirector findDirector(String email, Dissolution dissolution) {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .filter(director -> director.getEmail().equals(email))
                .findFirst()
                .get();
    }

    private void addDirectorApproval(String userId, String ip, String email, Dissolution dissolution) {
        final DirectorApproval approval = approvalMapper.mapToDirectorApproval(userId, ip);
        DissolutionDirector director = this.findDirector(email, dissolution);
        director.setDirectorApproval(approval);
    }

    private boolean hasDirectorsLeftToApprove(Dissolution dissolution) {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .anyMatch(director -> director.getDirectorApproval() == null);
    }

    private void setDissolutionToPendingPayment(Dissolution dissolution) {
        dissolution
                .getData()
                .getApplication()
                .setStatus(ApplicationStatus.PENDING_PAYMENT);
    }
}
