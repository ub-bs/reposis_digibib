package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.Date;
import java.util.List;

import org.mycore.datamodel.metadata.MCRObjectID;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequest.RequestStatus;

/**
 * Dto for {@link ContactRequest}.
 */
public record ContactRequestDto(@JsonProperty("id") String id, @JsonProperty("objectId") MCRObjectID objectId,
    @JsonProperty("body") ContactRequestBodyDto body, @JsonProperty("created") Date created,
    @JsonProperty("state") RequestStatus state, @JsonProperty("comment") String comment,
    @JsonProperty("contactPersons") List<ContactPersonDto> contactPersons) {
}
