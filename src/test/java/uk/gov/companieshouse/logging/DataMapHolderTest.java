package uk.gov.companieshouse.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(MockitoExtension.class)
public class DataMapHolderTest {

    private static final String REQUEST_ID = "test-request-id";

    @BeforeEach
    public void setUp() {
        DataMapHolder.initialise(REQUEST_ID);
    }

    @AfterEach
    public void tearDown() {
        DataMapHolder.clear();
    }

    @Test
    void testInitialisationSuccess() {
        assertThat(DataMapHolder.getRequestId(), is(REQUEST_ID));
    }

    @Test
    void testClearDownSuccess() {
        DataMapHolder.clear();
        assertThat(DataMapHolder.getRequestId(), is("uninitialised"));
    }

    @Test
    void testInitialiseWithNewRequestId() {
        DataMapHolder.initialise("new-request-id");
        assertThat(DataMapHolder.getRequestId(), is("new-request-id"));
    }

    @Test
    void testGetRequestIdFromLogMap() {
        assertThat(DataMapHolder.getLogMap().get("request_id"), is(REQUEST_ID));
    }

    @Test
    void testGetMissingValueFromLogMap() {
        assertThat(DataMapHolder.getLogMap().get("random_key"), is(nullValue()));
    }
    
    @Test
    void testUpdateLogMap() {
        DataMapHolder.getLogMap().put("key", "value");
        assertThat(DataMapHolder.getLogMap().get("key"), is(nullValue()));
    }
}
