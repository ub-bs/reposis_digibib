package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.Contact;

/**
 * Dto to update {@link Contact}.
 */
public record ContactUpdateDto(@JsonProperty("name") String name, @JsonProperty("email") String email,
    @JsonProperty("origin") String origin, @JsonProperty("reference") String reference,
    @JsonProperty("events") List<ContactEventDto> events) {
}
