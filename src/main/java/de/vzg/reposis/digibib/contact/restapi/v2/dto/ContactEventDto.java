package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactEvent;

/**
 * Dto for {@link ContactEvent}.
 */
public record ContactEventDto(@JsonProperty("date") Date date,
    @JsonProperty("type") ContactEvent.EventType type, @JsonProperty("comment") String comment) {
}
