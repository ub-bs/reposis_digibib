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

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactRequest;

/**
 * Dto to update {@link ContactRequest}.
 *
 * @param objectId object id
 * @param status request status
 * @param created date of creation
 * @param body request body
 * @param contacts list over contact update dto elements
 * @param comment comment
 */
public record ContactRequestUpdateDto(@JsonProperty("objectId") String objectId,
    @JsonProperty("state") ContactRequest.RequestStatus status, @JsonProperty("created") Date created,
    @JsonProperty("body") ContactRequestBodyDto body, @JsonProperty("contacts") List<ContactUpdateDto> contacts,
    @JsonProperty("comment") String comment) {
}
