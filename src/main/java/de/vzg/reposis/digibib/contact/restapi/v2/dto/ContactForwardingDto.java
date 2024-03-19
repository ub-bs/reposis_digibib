package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactForwarding;

/**
 * Dto for {@link ContactForwarding}.
 */
public record ContactForwardingDto(@JsonProperty("date") Date date, @JsonProperty("failed") Date failed,
    @JsonProperty("success") Date success) {
}
