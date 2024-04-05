package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactPerson;

/**
 * Dto for {@link ContactPerson}.
 */
public record ContactPersonDto(@JsonProperty("name") String name, @JsonProperty("email") String email,
    @JsonProperty("origin") String origin, @JsonProperty("events") List<ContactPersonEventDto> events) {
}
