package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactPerson;

/**
 * Dto to update {@link ContactPerson}.
 */
public record ContactPersonUpdateDto(@JsonProperty("name") String name, @JsonProperty("email") String email,
    @JsonProperty("origin") String origin, @JsonProperty("enabled") boolean enabled,
    @JsonProperty("forwarding") ContactForwardingDto forwarding) {
}
