package de.vzg.reposis.digibib.contact.model;

import java.util.Date;

/**
 * Defines contact event model.
 */
public record ContactEvent(EventType type, Date date, String comment) {

    /**
     * Defines event types.
     */
    public static enum EventType {
        SENT, SENT_FAILED, CONFIRMED
    }
}
