package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.chips.ChipsForm;
import uk.gov.companieshouse.model.dto.chips.ChipsFormAttachment;
import uk.gov.companieshouse.model.dto.chips.ChipsPackageMetadata;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;
import uk.gov.companieshouse.model.dto.chips.RejectReason;
import uk.gov.companieshouse.model.enums.VerdictResult;

import java.util.Collections;

public class ChipsFixtures {

    public static DissolutionChipsRequest generateDissolutionChipsRequest() {
        final DissolutionChipsRequest request = new DissolutionChipsRequest();

        request.setPackageMetadata(generateChipsPackageMetadata());
        request.setForms(Collections.singletonList(generateChipsForm()));

        return request;
    }

    public static ChipsPackageMetadata generateChipsPackageMetadata() {
        final ChipsPackageMetadata packageMetadata = new ChipsPackageMetadata();

        packageMetadata.setId("123");
        packageMetadata.setCount(1);

        return packageMetadata;
    }

    public static ChipsForm generateChipsForm() {
        final ChipsForm form = new ChipsForm();

        form.setBarcode("B4RC0D3");
        form.setXml("some xml contents");
        form.setAttachments(Collections.singletonList(generateChipsFormAttachment()));

        return form;
    }

    public static ChipsFormAttachment generateChipsFormAttachment() {
        final ChipsFormAttachment attachment = new ChipsFormAttachment();

        attachment.setMimeType("application/pdf");
        attachment.setCategory("FORM IMAGE PDF");
        attachment.setData("some certificate data");

        return attachment;
    }

    public static ChipsResponseCreateRequest generateChipsResponseCreateRequest() {
        final ChipsResponseCreateRequest chipsResponseCreateRequest = new ChipsResponseCreateRequest();

        chipsResponseCreateRequest.setBarcode("DEF789");
        chipsResponseCreateRequest.setSubmissionReference("XYZ456");
        chipsResponseCreateRequest.setStatus(VerdictResult.ACCEPTED);

        return chipsResponseCreateRequest;
    }

    public static RejectReason generateChipsRejectReason() {
        final RejectReason rejectReason = new RejectReason();

        rejectReason.setId("1");
        rejectReason.setDescription("some description");
        rejectReason.setTextEnglish("some reject reason");
        rejectReason.setTextWelsh("some welsh reject reason");
        rejectReason.setOrder(0);

        return rejectReason;
    }
}
