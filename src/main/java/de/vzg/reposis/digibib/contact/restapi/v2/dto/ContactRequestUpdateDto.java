package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactRequest;

/**
 * Dto to update {@link ContactRequest}.
 */
public record ContactRequestUpdateDto(@JsonProperty("comment") String comment) {
}
