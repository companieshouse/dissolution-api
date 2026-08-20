package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.DissolutionUserData;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateDraftResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.barcode.BarcodeGenerator;

import java.util.Map;

@Service
public class DissolutionCreator {

    private final ReferenceGenerator referenceGenerator;
    private final BarcodeGenerator barcodeGenerator;
    private final DissolutionRequestMapper requestMapper;
    private final DissolutionRepository repository;
    private final DissolutionResponseMapper responseMapper;
    private final DissolutionEmailService emailService;

    @Autowired
    public DissolutionCreator(
            ReferenceGenerator referenceGenerator,
            BarcodeGenerator barcodeGenerator,
            DissolutionRequestMapper requestMapper,
            DissolutionRepository repository,
            DissolutionResponseMapper responseMapper,
            DissolutionEmailService emailService) {
        this.referenceGenerator = referenceGenerator;
        this.barcodeGenerator = barcodeGenerator;
        this.requestMapper = requestMapper;
        this.repository = repository;
        this.responseMapper = responseMapper;
        this.emailService = emailService;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, CompanyProfile companyProfile, Map<String, CompanyOfficer> directors, DissolutionUserData userData) {
        final String reference = referenceGenerator.generateApplicationReference();
        final String barcode = barcodeGenerator.generateBarcode();
        final Dissolution dissolution = requestMapper.mapToDissolution(body, companyProfile, directors, userData, reference, barcode);
        repository.insert(dissolution);

        emailService.notifySignatoriesToSign(dissolution);
        return responseMapper.mapToDissolutionCreateResponse(dissolution);
    }

    public DissolutionCreateDraftResponse createDraft(Transaction transaction, CompanyProfile companyProfile, DissolutionUserData userData) {
        final Dissolution dissolution = requestMapper.mapToDraftDissolution(transaction, companyProfile, userData);
        repository.insert(dissolution);

        return responseMapper.mapToDissolutionCreateDraftResponse(transaction, dissolution);
    }
}
