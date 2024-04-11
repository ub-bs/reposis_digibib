package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactRequest;

/**
 * Dto to update {@link ContactRequest}.
 */
public record ContactRequestUpdateDto(@JsonProperty("objectId") String objectId,
    @JsonProperty("state") ContactRequest.RequestStatus status, @JsonProperty("created") Date created,
    @JsonProperty("body") ContactRequestBodyDto body, @JsonProperty("contacts") List<ContactUpdateDto> contacts,
    @JsonProperty("comment") String comment) {
}
