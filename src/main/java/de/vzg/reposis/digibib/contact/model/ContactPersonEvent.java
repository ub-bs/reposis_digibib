package de.vzg.reposis.digibib.contact.model;

import java.util.Date;

/**
 * Defines contact person event model.
 */
public record ContactPersonEvent(Date date, EventType type, String comment) {

    /**
     * Defines event types.
     */
    public static enum EventType {
        SENT, SENT_FAILED, CONFIRMED
    }
}
