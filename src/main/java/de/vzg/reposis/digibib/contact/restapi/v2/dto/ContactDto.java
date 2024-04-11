package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.Contact;

/**
 * Dto for {@link Contact}.
 */
public record ContactDto(@JsonProperty("name") String name, @JsonProperty("email") String email,
    @JsonProperty("origin") String origin, @JsonProperty("events") List<ContactEventDto> events) {
}
