package uk.gov.companieshouse.mapper.chips;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.chips.ChipsForm;
import uk.gov.companieshouse.model.dto.chips.ChipsFormAttachment;
import uk.gov.companieshouse.model.dto.chips.ChipsPackageMetadata;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;

import java.util.Base64;
import java.util.Collections;

import static uk.gov.companieshouse.model.Constants.CONTENT_TYPE_PDF;

@Service
public class DissolutionChipsMapper {

    private static final int PACKAGE_METADATA_COUNT = 1;

    private static final String FORM_CATEGORY = "FORM IMAGE PDF";

    private final ChipsFormDataMapper formDataMapper;

    public DissolutionChipsMapper(ChipsFormDataMapper formDataMapper) {
        this.formDataMapper = formDataMapper;
    }

    public DissolutionChipsRequest mapToDissolutionChipsRequest(Dissolution dissolution, byte[] certificate) {
        final DissolutionChipsRequest request = new DissolutionChipsRequest();

        request.setPackageMetadata(mapToPackageMetadata(dissolution));
        request.setForms(Collections.singletonList(mapToChipsForm(dissolution, certificate)));

        return request;
    }

    private ChipsPackageMetadata mapToPackageMetadata(Dissolution dissolution) {
        final ChipsPackageMetadata metadata = new ChipsPackageMetadata();

        metadata.setId(dissolution.getData().getApplication().getReference());
        metadata.setCount(PACKAGE_METADATA_COUNT);

        return metadata;
    }

    private ChipsForm mapToChipsForm(Dissolution dissolution, byte[] certificate) {
        final ChipsForm form = new ChipsForm();

        form.setBarcode(dissolution.getData().getApplication().getBarcode());
        form.setXml(encode(formDataMapper.mapToChipsFormDataXml(dissolution).getBytes()));
        form.setAttachments(Collections.singletonList(mapToChipsFormAttachment(certificate)));

        return form;
    }

    private ChipsFormAttachment mapToChipsFormAttachment(byte[] certificate) {
        final ChipsFormAttachment attachment = new ChipsFormAttachment();

        attachment.setCategory(FORM_CATEGORY);
        attachment.setMimeType(CONTENT_TYPE_PDF);
        attachment.setData(encode(certificate));

        return attachment;
    }

    private String encode(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }
}
