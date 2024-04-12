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

import de.vzg.reposis.digibib.contact.exception.ContactAlreadyExistsException;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.ContactInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.model.Contact;
import de.vzg.reposis.digibib.contact.model.ContactEvent;
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
     * Returns {@link Contact} by given id.
     *
     * @param requestId request id
     * @param email email
     * @return contact person
     * @throws ContactNotFoundException if contact does not exist
     */
    Contact getContactByEmail(UUID requestId, String email);

    /**
     * Adds {@link Contact} to request by id.
     *
     * @param requestId request id
     * @param contact contact
     * @throws ContactInvalidException if contact is invalid
     * @throws ContactAlreadyExistsException if contact already exists
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     */
    void addContact(UUID requestId, Contact contact);

    /**
     * Updates {@link Contact} of request by id.
     *
     * @param requestId request id
     * @param contact contact
     * @throws ContactInvalidException if contact is invalid
     * @throws ContactAlreadyExistsException if contact already exists
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     */
    void updateContact(UUID requestId, Contact contact);

    /**
     * Removes {@link Contact} of request by given id.
     *
     * @param requestId request id
     * @param email email
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactNotFoundException if contact cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     */
    void deleteContactByEmail(UUID requestId, String email);

    /**
     * Confirms request as confirmed by specified {@link Contact}.
     *
     * @param requestId request id
     * @param email contact mail
     * @param event event
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactNotFoundException if contact cannot be found
     */
    void addContactEvent(UUID requestId, String email, ContactEvent event);

    /**
     * Collects contact persons for request.
     *
     * @param requestId request id
     * @throws ContactRequestNotFoundException if request does not exist
     */
    void collectContacts(UUID requestId);

    /**
     * Forwards request to specific {@link Contact}.
     *
     * @param requestId request id
     * @param email contact mail
     * @throws ContactNotFoundException if contact cannot be found
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     * @throws MCRException if sending failed
     */
    void forwardRequest(UUID requestId, String email);

    /**
     * Discovers and handles bounce messages.
     *
     * @throws ContactException if cannot retrieve bounced message
     */
    void handleBouncedMessages();

}
