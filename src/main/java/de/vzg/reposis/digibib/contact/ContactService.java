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

package de.vzg.reposis.digibib.contact;

import java.util.Collection;
import java.util.UUID;

import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;

import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonAlreadyExistsException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonOriginException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonStateException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactPersonEvent;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;

public interface ContactService {

    /**
     * Returns a collection of all {@link ContactRequest} elements.
     *
     * @return collection of contact request elements
     */
    Collection<ContactRequest> listAllRequests();

    /**
     * Returns {@link ContactRequest} by given id.
     *
     * @param requestId request id
     * @return request
     * @throws ContactRequestNotFoundException if request does not exist
     */
    ContactRequest getRequest(UUID requestId);

    /**
     * Adds a new {@link ContactRequest}.
     *
     * @param requestBody request body
     * @throws ContactRequestInvalidException if the request is invalid
     */
    void createRequest(MCRObjectID objectId, ContactRequestBody requestBody);

    /**
     * Updates {@link ContactRequest}.
     *
     * @param request request
     * @throws ContactRequestNotFoundException if contact request does not exist
     */
    void updateRequest(ContactRequest request);

    /**
     * Removes {@link ContactRequest} by id.
     *
     * @param requestId request id
     * @throws ContactRequestNotFoundException if request does not exist
     * @throws ContactRequestStateException if request is in wrong state
     */
    void deleteRequest(UUID requestId);

    /**
     * Returns {@link ContactPerson} by given id.
     *
     * @param requestId request id
     * @param mail mail
     * @return contact person
     * @throws ContactPersonNotFoundException if contact person does not exist
     */
    ContactPerson getContactPerson(UUID requestId, String mail);

    /**
     * Adds {@link ContactPerson} to request by id.
     * Only person origin manual is allowed.
     *
     * @param requestId request id
     * @param contactPerson contact person
     * @return id
     * @throws ContactPersonInvalidException if contact person is invalid
     * @throws ContactPersonAlreadyExistsException if contact person with given mail already exists
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     * @throws ContactPersonOriginException if contact person has wrong origin
     */
    void addContactPerson(UUID requestId, ContactPerson contactPerson);

    /**
     * Updates {@link ContactPerson} of request by id.
     *
     * @param requestId request id
     * @param contactPerson contact person
     * @throws ContactPersonInvalidException if contact person is invalid
     * @throws ContactPersonAlreadyExistsException if person with given mail already exists
     * @throws ContactPersonOriginException if person does not exists
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     * @throws ContactPersonOriginException if request has wrong origin
     */
    void updateContactPerson(UUID requestId, ContactPerson contactPerson);

    /**
     * Removes {@link ContactPerson} of request by given id.
     *
     * @param requestId request id
     * @param mail contact person mail
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactPersonNotFoundException if contact person cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     * @throws ContactPersonOriginException if request has wrong origin
     */
    void deleteContactPerson(UUID requestId, String mail);

    /**
     * Collects contact persons for request.
     *
     * @param requestId request id
     * @throws ContactRequestNotFoundException if request does not exist
     */
    void collectContactPersons(UUID requestId);

    /**
     * Forwards request to specific {@link ContactPerson}.
     *
     * @param requestId request id
     * @param mail contact person mail
     * @throws ContactPersonNotFoundException if contact person cannot be found
     * @throws ContactPersonStateException if contact person is in wrong state
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     * @throws MCRException if sending failed
     */
    void forwardRequest(UUID requestId, String mail);

    /**
     * Discovers and handles bounce messages.
     *
     * @throws ContactException if cannot retrieve bounced message
     */
    void handleBouncedMessages();

    /**
     * Confirms request as confirmed by specified {@link ContactPerson}.
     *
     * @param requestId request id
     * @param mail contact person mail
     * @param event event
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactPersonNotFoundException if contact person cannot be found
     */
    void addPersonEvent(UUID requestId, String mail, ContactPersonEvent event);
}
