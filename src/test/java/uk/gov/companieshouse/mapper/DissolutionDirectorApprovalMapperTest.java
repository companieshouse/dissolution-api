package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalCommand;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionPatchRequest;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DissolutionDirectorApprovalMapperTest {
    private static final String USER_ID = "user123";

    private final DissolutionDirectorApprovalMapper mapper = Mappers.getMapper(DissolutionDirectorApprovalMapper.class);

    @Test
    void when_all_required_properties_are_provided_then_map_to_command() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        final DissolutionDirectorApprovalCommand result = mapper.toCommand(USER_ID, body);

        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.hasApproved()).isEqualTo(body.getHasApproved());
        assertThat(result.officerId()).isEqualTo(body.getOfficerId());
        assertThat(result.ipAddress()).isEqualTo(body.getIpAddress());
    }

    @Test
    void when_invariants_are_violated_then_throw_exception() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        assertThatThrownBy(() -> mapper.toCommand(null, body)).isInstanceOf(NullPointerException.class);
    }
}
