package com.example.eventlottery.utils;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * Unit tests for {@link QRCodeHelper} deep-link helper behaviour.
 *
 * Purpose:
 * Confirms that QR-related helper methods generate and parse event deep links
 * in the expected format used throughout the application.
 *
 * Role in application:
 * Supports QR-code based event discovery so entrants can scan a promotional
 * code and navigate directly to an event detail page.
 *
 * Outstanding issues:
 * These tests currently focus on core happy-path and invalid-link parsing only;
 * additional malformed input cases could be added in the future.
 */
public class QRCodeHelperTest {
    /**
     * Verifies that {@link QRCodeHelper#buildEventDeepLink(String)} produces the
     * expected application deep-link URI for a given event identifier.
     */
    @Test
    public void testBuildEventDeepLink() {
        String eventId = "test_event_123";
        String expected = "eventlottery://event/test_event_123";
        assertEquals(expected, QRCodeHelper.buildEventDeepLink(eventId));
    }
    /**
     * Verifies that {@link QRCodeHelper#extractEventId(String)} returns the
     * embedded event identifier for a valid deep link and returns {@code null}
     * for invalid or missing links.
     */
    @Test
    public void testExtractEventId() {
        String deepLink = "eventlottery://event/my_id_999";
        assertEquals("my_id_999", QRCodeHelper.extractEventId(deepLink));

        assertNull(QRCodeHelper.extractEventId("invalid_link"));
        assertNull(QRCodeHelper.extractEventId(null));
    }
}
