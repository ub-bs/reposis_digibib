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

import de.vzg.reposis.digibib.contact.model.ContactForwarding;
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactForwardingDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactPersonCreateDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactPersonDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactPersonUpdateDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactRequestBodyDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactRequestDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactRequestUpdateDto;

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
        final ContactRequest request = new ContactRequest();
        request.setComment(requestDto.comment());
        return request;
    }

    /**
     * Maps and returns {@link ContactRequest} to {@link ContactRequestDto}.
     *
     * @param request request
     * @return request dto
     */
    protected static ContactRequestDto toDto(ContactRequest request) {
        final List<ContactPersonDto> personDtos
            = request.getContactPersons().stream().map(ContactRestHelper::toDto).toList();
        final ContactRequestBodyDto bodyDto = toDto(request.getBody());
        return new ContactRequestDto(request.getId().toString(), request.getObjectId(), bodyDto, request.getCreated(),
            request.getForwarded(), request.getState(), request.getComment(), personDtos);
    }

    private static ContactRequestBodyDto toDto(ContactRequestBody body) {
        return new ContactRequestBodyDto(body.fromName(), body.fromMail(), body.fromOrcid(), body.message());
    }

    /**
     * Maps and returns {@link ContactPersonUpdateDto} to {@link ContactPerson}.
     *
     * @param personDto person dto
     * @return person
     */
    protected static ContactPerson toDomain(ContactPersonUpdateDto personDto) {
        final ContactPerson person = new ContactPerson(personDto.name(), personDto.email(), personDto.origin());
        person.setEnabled(personDto.enabled());
        Optional.ofNullable(personDto.forwarding()).map(ContactRestHelper::toDomain).ifPresent(person::setForwarding);
        return person;
    }

    private static ContactForwarding toDomain(ContactForwardingDto forwardingDto) {
        final ContactForwarding forwarding = new ContactForwarding();
        forwarding.setConfirmed(forwardingDto.success());
        forwarding.setDate(forwardingDto.date());
        forwarding.setFailed(forwardingDto.failed());
        return forwarding;
    }

    /**
     * Maps and returns {@link ContactPerson} to {@link ContactPersonDto}.
     *
     * @param person person
     * @return person dto
     */
    protected static ContactPersonDto toDto(ContactPerson contactPerson) {
        final ContactForwardingDto forwardingDto
            = Optional.ofNullable(contactPerson.getForwarding()).map(ContactRestHelper::toDto).orElse(null);
        return new ContactPersonDto(contactPerson.getName(), contactPerson.getMail(), contactPerson.getOrigin(),
            contactPerson.isEnabled(), forwardingDto);
    }

    private static ContactForwardingDto toDto(ContactForwarding forwarding) {
        return new ContactForwardingDto(forwarding.getDate(), forwarding.getFailed(), forwarding.getFailed());
    }

    /**
     * Maps and returns {@link ContactPersonCreateDto} to {@link ContactPerson}.
     *
     * @param personDto person dto
     * @return person
     */
    protected static ContactPerson toDomain(ContactPersonCreateDto personDto) {
        final ContactPerson person = new ContactPerson(personDto.name(), personDto.email(), personDto.origin());
        person.setEnabled(personDto.enabled());
        return person;
    }

}
