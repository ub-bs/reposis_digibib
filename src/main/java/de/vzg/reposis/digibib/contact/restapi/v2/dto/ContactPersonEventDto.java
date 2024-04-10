package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactPersonEvent;

/**
 * Dto for {@link ContactPersonEvent}.
 */
public record ContactPersonEventDto(@JsonProperty("date") Date date,
    @JsonProperty("type") ContactPersonEvent.EventType type, @JsonProperty("comment") String comment) {
}
