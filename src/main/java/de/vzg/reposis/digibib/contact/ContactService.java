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

import de.vzg.reposis.digibib.contact.exception.ContactRecipientAlreadyExistsException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientOriginException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientStateException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequest;

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
     * @param request request
     * @throws ContactRequestInvalidException if the request is invalid
     */
    void addRequest(ContactRequest request);

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
     * @param requestId request if
     * @throws ContactRequestNotFoundException if request does not exist
     * @throws ContactRequestStateException    if request is in wrong state
     */
    void deleteRequest(UUID requestId);

    /**
     * Returns {@link ContactRecipient} by given id.
     *
     * @param recipientId recipient id
     * @return recipient
     * @throws ContactRecipientNotFoundException if recipient does not exist
     */
    ContactRecipient getRecipient(UUID recipientId);

    /**
     * Adds {@link ContactRecipient} to request by id.
     *
     * @param requestId request uuid
     * @param recipient recipient
     * @throws ContactRecipientInvalidException       if recipient is invalid
     * @throws ContactRecipientAlreadyExistsException if recipient with given mail
     *                                                already exists
     * @throws ContactRequestNotFoundException        if request cannot be found
     * @throws ContactRequestStateException           if request is in wrong state
     */
    void addRecipient(UUID requestId, ContactRecipient recipient);

    /**
     * Updates {@link ContactRecipient} of request by id.
     *
     * @param requestId   request id
     * @param recipient   recipient
     * @throws ContactRecipientInvalidException       if recipient is invalid
     * @throws ContactRecipientAlreadyExistsException if recipient with given mail
     *                                                already exists
     * @throws ContactRecipientOriginException if recipeint does not exists
     * @throws ContactRequestNotFoundException        if request cannot be found
     * @throws ContactRequestStateException           if request is in wrong state
     * @throws ContactRecipientOriginException        if request has wrong origin
     */
    void updateRecipient(UUID requestId, ContactRecipient recipient);

    /**
     * Removes recipient of request by given id.
     *
     * @param requestId   the request uuid
     * @param recipientId the recipient uuid
     * @throws ContactRequestNotFoundException   if request cannot be found
     * @throws ContactRecipientNotFoundException if recipient cannot be found
     * @throws ContactRequestStateException      if request is in wrong state
     * @throws ContactRecipientOriginException   if request has wrong origin
     */
    void deleteRecipient(UUID requestId, UUID recipientId);

    /**
     * Confirms request as confirmed by specified recipient
     *
     * @param requestId   the request id
     * @param recipientId the recipient id
     * @throws ContactRequestNotFoundException   if request cannot be found
     * @throws ContactRecipientNotFoundException if recipient cannot be found
     */
    void confirmRequest(UUID requestId, UUID recipientId);

    /**
     * Forwards request to recipients in a separate thread.
     *
     * @param requestId request id
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException    if request is in wrong state
     */
    void forwardRequest(UUID requestId);

    /**
     * Forwards request to recipient in a separate thread.
     *
     * @param requestId  request id
     * @param recipientId recipient id
     * @throws ContactRecipientNotFoundException if recipient cannot be found
     * @throws ContactRecipientStateException    if recipient is in wrong state
     * @throws ContactRequestNotFoundException   if request cannot be found
     * @throws ContactRequestStateException      if request is in wrong state
     * @throws MCRException                      if sending failed
     */
    void forwardRequest(UUID requestId, UUID recipientId);
}
