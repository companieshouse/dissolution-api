package uk.gov.companieshouse.mapper;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.ChipsFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.model.enums.VerdictResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DissolutionVerdictMapperTest {

    private final DissolutionVerdictMapper dissolutionVerdictMapper = new DissolutionVerdictMapper();

    @Test
    public void mapToDissolutionVerdict_mapsToAcceptedDissolutionVerdict() {
        ChipsResponseCreateRequest chipsResponseCreateRequest = ChipsFixtures.generateChipsResponseCreateRequest();

        final DissolutionVerdict dissolutionVerdict = dissolutionVerdictMapper.mapToDissolutionVerdict(chipsResponseCreateRequest);

        assertEquals(VerdictResult.ACCEPTED, dissolutionVerdict.getResult());
        assertNull(dissolutionVerdict.getRejectReasons());
        assertNotNull(dissolutionVerdict.getDateTime());
    }

    @Test
    public void mapToDissolutionVerdict_mapsToRejectedDissolutionVerdict() {
        ChipsResponseCreateRequest chipsResponseCreateRequest = ChipsFixtures.generateChipsResponseCreateRequest();
        chipsResponseCreateRequest.setStatus(VerdictResult.REJECTED);
        chipsResponseCreateRequest.setRejectReasons(Arrays.array(ChipsFixtures.generateChipsRejectReason()));

        final DissolutionVerdict dissolutionVerdict = dissolutionVerdictMapper.mapToDissolutionVerdict(chipsResponseCreateRequest);

        assertEquals(VerdictResult.REJECTED, dissolutionVerdict.getResult());
        assertEquals(DissolutionFixtures.generateDissolutionRejectReason().getId(), dissolutionVerdict.getRejectReasons().get(0).getId());
        assertEquals(DissolutionFixtures.generateDissolutionRejectReason().getDescription(), dissolutionVerdict.getRejectReasons().get(0).getDescription());
        assertEquals(DissolutionFixtures.generateDissolutionRejectReason().getTextEnglish(), dissolutionVerdict.getRejectReasons().get(0).getTextEnglish());
        assertNotNull(dissolutionVerdict.getDateTime());
    }
}
