package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionUserDataMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.DissolutionUserData;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
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
    private final DissolutionUserDataMapper userDataMapper;
    private final Logger logger;

    @Autowired
    public DissolutionCreator(
            ReferenceGenerator referenceGenerator,
            BarcodeGenerator barcodeGenerator,
            DissolutionRequestMapper requestMapper,
            DissolutionRepository repository,
            DissolutionResponseMapper responseMapper,
            DissolutionEmailService emailService,
            DissolutionUserDataMapper userDataMapper, Logger logger) {
        this.referenceGenerator = referenceGenerator;
        this.barcodeGenerator = barcodeGenerator;
        this.requestMapper = requestMapper;
        this.repository = repository;
        this.responseMapper = responseMapper;
        this.emailService = emailService;
        this.userDataMapper = userDataMapper;
        this.logger = logger;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, CompanyProfile companyProfile, Map<String, CompanyOfficer> directors, String userId, String ip, String email) {
        final String reference = referenceGenerator.generateApplicationReference();

        logger.info("Reference Generator: " + reference);

        final String barcode = barcodeGenerator.generateBarcode();

        logger.info("Barcode Generator: " + barcode);

        final DissolutionUserData userData = userDataMapper.mapToUserData(userId, ip, email);

        logger.info("User data mapper: " + userData);

        final Dissolution dissolution = requestMapper.mapToDissolution(body, companyProfile, directors, userData, reference, barcode);

        logger.info("Request Mapper: " + dissolution);

        repository.insert(dissolution);

        logger.info("Insert complete");

        emailService.notifySignatoriesToSign(dissolution);

        logger.info("Email sent");

        return responseMapper.mapToDissolutionCreateResponse(dissolution);
    }
}
