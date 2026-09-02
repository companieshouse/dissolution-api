package uk.gov.companieshouse.model.db.dissolution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;

@DisplayNameGeneration(ReplaceUnderscores.class)
class DissolutionDirectorTest {

    @Nested
    class hasDetailsChanged {

        private DissolutionDirector director;

        @BeforeEach
        void initialize() {
            director = new DissolutionDirector();
            director.setEmail("existing@email.com");
            director.setOnBehalfName("existing name");
        }

        @Test
        void when_email_has_changed_then_returns_true() {
            assertThat(director.hasDetailsChanged("new@email.com", "existing name")).isTrue();
        }

        @Test
        void when_email_has_mixed_casing_and_whitespace_then_returns_false() {
            assertThat(director.hasDetailsChanged("  EXISTING@EMAIL.COM  ", "existing name")).isFalse();
        }

        @Test
        void when_on_behalf_name_has_changed_then_returns_true() {
            assertThat(director.hasDetailsChanged("existing@email.com", "new name")).isTrue();
        }

        @Test
        void when_both_email_and_on_behalf_name_have_changed_then_returns_true() {
            assertThat(director.hasDetailsChanged("new@email.com", "new name")).isTrue();
        }

        @Test
        void when_neither_email_nor_on_behalf_name_have_changed_then_returns_false() {
            assertThat(director.hasDetailsChanged("existing@email.com", "existing name")).isFalse();
        }

        @Test
        void when_on_behalf_name_was_null_and_new_value_is_set_then_returns_true() {
            director.setOnBehalfName(null);
            assertThat(director.hasDetailsChanged("existing@email.com", "new name")).isTrue();
        }

        @Test
        void when_on_behalf_name_was_set_and_new_value_is_null_then_returns_true() {
            assertThat(director.hasDetailsChanged("existing@email.com", null)).isTrue();
        }

        @Test
        void when_both_on_behalf_names_are_null_then_returns_false() {
            director.setOnBehalfName(null);
            assertThat(director.hasDetailsChanged("existing@email.com", null)).isFalse();
        }
    }
}
