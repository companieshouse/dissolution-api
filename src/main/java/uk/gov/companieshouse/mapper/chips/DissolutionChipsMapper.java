package uk.gov.companieshouse.mapper.chips;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.chips.ChipsForm;
import uk.gov.companieshouse.model.dto.chips.ChipsFormAttachment;
import uk.gov.companieshouse.model.dto.chips.ChipsPackageMetadata;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;

import java.util.Base64;
import java.util.Collections;

@Service
public class DissolutionChipsMapper {

    private static final int PACKAGE_METADATA_COUNT = 1;

    private static final String FORM_CATEGORY = "FORM IMAGE PDF";
    private static final String FORM_MIME_TYPE = "application/pdf";

    private final ChipsFormDataMapper formDataMapper;

    public DissolutionChipsMapper(ChipsFormDataMapper formDataMapper) {
        this.formDataMapper = formDataMapper;
    }

    public DissolutionChipsRequest mapToDissolutionChipsRequest(Dissolution dissolution, String certificate) throws JsonProcessingException {
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

    private ChipsForm mapToChipsForm(Dissolution dissolution, String certificate) throws JsonProcessingException {
        final ChipsForm form = new ChipsForm();

        form.setBarcode(dissolution.getData().getApplication().getBarcode());
        form.setXml(encode(formDataMapper.mapToChipsFormDataXml(dissolution)));
        form.setAttachments(Collections.singletonList(mapToChipsFormAttachment(certificate)));

        return form;
    }

    private ChipsFormAttachment mapToChipsFormAttachment(String certificate) {
        final ChipsFormAttachment attachment = new ChipsFormAttachment();

        attachment.setCategory(FORM_CATEGORY);
        attachment.setMimeType(FORM_MIME_TYPE);
        attachment.setData(encode(certificate));

        return attachment;
    }

    private String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
}
