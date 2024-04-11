package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.Contact;

/**
 * Dto for {@link Contact} creation.
 */
public record ContactCreateDto(@JsonProperty("name") String name, @JsonProperty("email") String email,
    @JsonProperty("origin") String origin, @JsonProperty("reference") String reference) {
}
