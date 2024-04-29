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

package de.vzg.reposis.digibib.contact.restapi.v2;

import java.util.List;
import java.util.Optional;

import org.mycore.datamodel.metadata.MCRObjectID;

import de.vzg.reposis.digibib.contact.model.Contact;
import de.vzg.reposis.digibib.contact.model.ContactEvent;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactCreateDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactEventDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactRequestBodyDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactRequestDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactRequestUpdateDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactUpdateDto;

/**
 * Provides helper methods for rest api.
 */
public class ContactRestHelper {

    /**
     * Maps and returns {@link ContactRequestUpdateDto} to {@link ContactRequest}.
     *
     * @param requestDto request dto
     * @return request
     */
    protected static ContactRequest toDomain(ContactRequestUpdateDto requestDto) {
        final ContactRequest request = new ContactRequest(toDomain(requestDto.body()));
        requestDto.contacts().stream().map(ContactRestHelper::toDomain).forEach(request::addContact);
        request.setComment(requestDto.comment());
        request.setCreated(requestDto.created());
        request.setObjectId(MCRObjectID.getInstance(requestDto.objectId()));
        request.setStatus(requestDto.status());
        return request;
    }

    private static ContactRequestBody toDomain(ContactRequestBodyDto bodyDto) {
        return new ContactRequestBody(bodyDto.name(), bodyDto.email(), bodyDto.orcid(), bodyDto.message());
    }

    /**
     * Maps and returns {@link ContactRequest} to {@link ContactRequestDto}.
     *
     * @param request request
     * @return request dto
     */
    protected static ContactRequestDto toDto(ContactRequest request) {
        final List<ContactDto> contactsDtos = request.getContacts().stream().map(ContactRestHelper::toDto).toList();
        final ContactRequestBodyDto bodyDto = toDto(request.getBody());
        return new ContactRequestDto(request.getId().toString(), request.getObjectId(), request.getStatus(),
            request.getCreated(), bodyDto, contactsDtos, request.getComment());
    }

    private static ContactRequestBodyDto toDto(ContactRequestBody body) {
        return new ContactRequestBodyDto(body.name(), body.email(), body.orcid(), body.message());
    }

    /**
     * Maps and returns {@link ContactUpdateDto} to {@link Contact}.
     *
     * @param contactDto contact dto
     * @return contact
     */
    protected static Contact toDomain(ContactUpdateDto contactDto) {
        final Contact contact = new Contact(contactDto.name(), contactDto.email(), contactDto.origin(),
            contactDto.reference());
        Optional.ofNullable(contactDto.events()).ifPresent(e -> {
            e.stream().map(ContactRestHelper::toDomain).forEach(contact::addEvent);
        });
        return contact;
    }

    private static ContactEvent toDomain(ContactEventDto eventDto) {
        return new ContactEvent(eventDto.type(), eventDto.date(), eventDto.comment());
    }

    /**
     * Maps and returns {@link Contact} to {@link ContactDto}.
     *
     * @param contact contact
     * @return contact dto
     */
    protected static ContactDto toDto(Contact contact) {
        final List<ContactEventDto> events = contact.getEvents().stream().map(ContactRestHelper::toDto).toList();
        return new ContactDto(contact.getName(), contact.getEmail(), contact.getOrigin(), events);
    }

    private static ContactEventDto toDto(ContactEvent event) {
        return new ContactEventDto(event.type(), event.date(), event.comment());
    }

    /**
     * Maps and returns {@link ContactCreateDto} to {@link Contact}.
     *
     * @param contactDto contact dto
     * @return contact
     */
    protected static Contact toDomain(ContactCreateDto contactDto) {
        return new Contact(contactDto.name(), contactDto.email(), contactDto.origin(), contactDto.reference());
    }

}
