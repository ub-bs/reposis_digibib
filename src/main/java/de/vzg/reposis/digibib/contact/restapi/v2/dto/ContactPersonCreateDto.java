package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactPerson;

/**
 * Dto for {@link ContactPerson} creation.
 */
public record ContactPersonCreateDto(@JsonProperty("name") String name, @JsonProperty("email") String email,
    @JsonProperty("origin") String origin, @JsonProperty("enabled") boolean enabled) {
}
