package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.DissolutionRejectReason;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.chips.RejectReason;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.util.DateTimeGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DissolutionVerdictMapper {
    public DissolutionVerdict mapToDissolutionVerdict(ChipsResponseCreateRequest body) {
        DissolutionVerdict verdict = new DissolutionVerdict();

        verdict.setResult(body.getStatus());

        Optional
                .ofNullable(body.getRejectReasons())
                .ifPresent(rejectReasons -> setRejectReasons(rejectReasons, verdict));

        verdict.setDateTime(DateTimeGenerator.generateCurrentDateTime());

        return verdict;
    }

    private void setRejectReasons(RejectReason[] rejectReasons, DissolutionVerdict verdict) {
        List<DissolutionRejectReason> dissolutionRejectReasons = Arrays.stream(rejectReasons)
                .map(this::mapToDissolutionRejectReason).collect(Collectors.toList());

        verdict.setRejectReasons(dissolutionRejectReasons);
    }

    private DissolutionRejectReason mapToDissolutionRejectReason(RejectReason rejectReason) {
        final DissolutionRejectReason dissolutionRejectReason = new DissolutionRejectReason();

        dissolutionRejectReason.setId(rejectReason.getId());
        dissolutionRejectReason.setDescription(rejectReason.getDescription());
        dissolutionRejectReason.setTextEnglish(rejectReason.getTextEnglish());

        return dissolutionRejectReason;
    }
}
