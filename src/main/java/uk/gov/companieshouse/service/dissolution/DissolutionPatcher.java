package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DirectorApprovalMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.sql.Timestamp;

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

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, String userId, String ip, String email) {
        final Dissolution dissolution = this.repository.findByCompanyNumber(companyNumber).get();

        this.addDirectorApproval(userId, ip, email, dissolution);

        if (!this.hasDirectorsLeftToApprove(dissolution)) {
            dissolution
                    .getData()
                    .getApplication()
                    .setStatus(ApplicationStatus.PENDING_PAYMENT);
        }

        this.repository.save(dissolution);

        return this.responseMapper.mapToDissolutionPatchResponse(companyNumber);
    }

    public void updatePaymentStatus(String paymentReference, Timestamp paidAt, String companyNumber) {
        final Dissolution dissolution = this.repository.findByCompanyNumber(companyNumber).get();

        this.addPaymentInformation(paymentReference, paidAt, dissolution);

        dissolution
                .getData()
                .getApplication()
                .setStatus(ApplicationStatus.PAID);

        this.repository.save(dissolution);
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

    private void addPaymentInformation(String paymentReference, Timestamp paidAt, Dissolution dissolution) {
        final PaymentInformation information = new PaymentInformation();
        information.setMethod(PaymentMethod.CREDIT_CARD);
        information.setReference(paymentReference);
        information.setDateTime(paidAt.toLocalDateTime());

        dissolution.setPaymentInformation(information);
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
}
