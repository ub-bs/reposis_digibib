/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vzg.reposis.digibib.contact.restapi.v2.dto;

import java.util.Date;
import java.util.List;

import org.mycore.datamodel.metadata.MCRObjectID;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequest.RequestStatus;

/**
 * Dto for {@link ContactRequest}.
 *
 * @param id id
 * @param objectId object id
 * @param state request state
 * @param created date of creation
 * @param body request body
 * @param contacts list over contact elements
 * @param comment comment
 */
public record ContactRequestDto(@JsonProperty("id") String id, @JsonProperty("objectId") MCRObjectID objectId,
    @JsonProperty("state") RequestStatus state, @JsonProperty("created") Date created,
    @JsonProperty("body") ContactRequestBodyDto body, @JsonProperty("contacts") List<ContactDto> contacts,
    @JsonProperty("comment") String comment) {
}
