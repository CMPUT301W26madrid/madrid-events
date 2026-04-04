package com.example.eventlottery.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class QRCodeHelperTest {

    @Test
    public void testBuildEventDeepLink() {
        String eventId = "test_event_123";
        String expected = "eventlottery://event/test_event_123";
        assertEquals(expected, QRCodeHelper.buildEventDeepLink(eventId));
    }

    @Test
    public void testExtractEventId() {
        String deepLink = "eventlottery://event/my_id_999";
        assertEquals("my_id_999", QRCodeHelper.extractEventId(deepLink));

        assertNull(QRCodeHelper.extractEventId("invalid_link"));
        assertNull(QRCodeHelper.extractEventId(null));
    }
}
