package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactRequestBody;

/**
 * Dto for {@link ContactRequestBody}.
 */
public record ContactRequestBodyDto(@JsonProperty("name") String name, @JsonProperty("email") String email,
    @JsonProperty("orcid") String orcid, @JsonProperty("message") String message) {
}
