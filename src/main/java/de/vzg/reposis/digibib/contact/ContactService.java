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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.vzg.reposis.digibib.contact.dao.ContactRecipientDAO;
import de.vzg.reposis.digibib.contact.dao.ContactRecipientDAOImpl;
import de.vzg.reposis.digibib.contact.dao.ContactRequestDAO;
import de.vzg.reposis.digibib.contact.dao.ContactRequestDAOImpl;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientAlreadyExistsException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientOriginException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.validation.ContactValidator;

import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ContactService {

    private final Lock readLock;

    private final Lock writeLock;

    /**
     * Request dao.
     */
    private final ContactRequestDAO requestDAO;

    /**
     * Recipient dao.
     */
    private final ContactRecipientDAO recipientDAO;

    private ContactService() {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        requestDAO = new ContactRequestDAOImpl();
        recipientDAO = new ContactRecipientDAOImpl();
    }

    public static ContactService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Lists all contact requests.
     * @return the requests as list
     */
    public List<ContactRequest> listRequests() {
        try {
            readLock.lock();
            return List.copyOf(requestDAO.findAll());
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Lists all contact requests with given state.
     * @param state the state
     * @return the requests as list
     */
    public List<ContactRequest> listRequestsByState(ContactRequestState state) {
        try {
            readLock.lock();
            return new ArrayList(requestDAO.findByState(state));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a contact request by given uuid.
     * @param uuid the uuid
     * @return the requests or null
     */
    public ContactRequest getRequestByUUID(UUID uuid) {
        try {
            readLock.lock();
            return requestDAO.findByUUID(uuid);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Creates a new contact requests.
     * @param request the contact request
     * @throws ContactRequestInvalidException if the request is invalid
     */
    public void insertRequest(ContactRequest request) throws ContactRequestInvalidException {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateRequest(request)) {
                throw new ContactRequestInvalidException();
            }
            final MCRObjectID objectID = request.getObjectID();
            if (objectID == null || !MCRMetadataManager.exists(objectID)) {
                throw new ContactRequestInvalidException(objectID.toString() + " does not exist.");
            }
            final Date currentDate = new Date();
            request.setCreated(currentDate);
            request.setLastModified(currentDate);
            final String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
            request.setRecipients(new ArrayList()); // sanitize recipients
            request.setCreatedBy(currentUserID);
            request.setLastModifiedBy(currentUserID);
            request.setState(ContactRequestState.RECEIVED);
            requestDAO.insert(request);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates a contact request
     * @param request the request
     * @throws ContactRequestNotFoundException if contact request does not exist.
     */
    public void updateRequest(ContactRequest request) throws ContactRequestNotFoundException {
        try {
            writeLock.lock();
            if (requestDAO.findByID(request.getId()) != null) {
                update(request);
            } else {
                throw new ContactRequestNotFoundException();
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes a contact request by given uuid.
     * @param uuid the request uuid
     * @throws ContactRequestNotFoundException if request cannot be found
     */
    public void removeRequestByUUID(UUID requestUUID) throws ContactRequestNotFoundException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            requestDAO.remove(request);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Forwards request to recipients in a separate thread.
     * @param requestUUID the request uuid
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     */
    public void forwardRequestByUUID(UUID requestUUID) throws ContactRequestNotFoundException,
            ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!(ContactRequestState.PROCESSED.equals(request.getState())
                    || ContactRequestState.SENDING_FAILED.equals(request.getState()))) {
                throw new ContactRequestStateException("Contact request state is not ready.");
            }
            request.setState(ContactRequestState.SENDING);
            update(request);
            Thread thread = new Thread(new ContactForwardRequestTask(request));
            thread.start();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Confirms request as confirmed by specified recipient
     * @param requestUUID the request uuid
     * @param recipientUUID the recipient uuid
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRecipientNotFoundException if recipient cannot be found
     */
    public void confirmRequestByUUID(UUID requestUUID, UUID recipientUUID) throws ContactRequestNotFoundException,
            ContactRecipientNotFoundException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            final ContactRecipient recipient = request.getRecipients().stream().filter(r -> r.getUUID().equals(recipientUUID))
                    .findFirst().orElseThrow(() -> new ContactRecipientNotFoundException());
            recipient.setConfirmed(new Date());
            recipientDAO.update(recipient);
            request.setState(ContactRequestState.CONFIRMED);
            update(request); // update modified
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Adds recipient to given request uuid
     * @param requestUUID the request uuid
     * @param recipient the recipient
     * @throws ContactRecipientInvalidException if recipient is invalid
     * @throws ContactRecipientAlreadyExistsException if recipient with given mail already exists
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     */
    public void addRecipient(UUID requestUUID, ContactRecipient recipient) throws ContactRecipientInvalidException,
            ContactRequestNotFoundException, ContactRequestStateException, ContactRecipientAlreadyExistsException {
        try {
            writeLock.lock();
            if (!ContactRecipientOrigin.MANUAL.equals(recipient.getOrigin())) {
                throw new ContactRecipientOriginException();
            }
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!isWarmState(request.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            if (!ContactValidator.getInstance().validateRecipient(recipient)) {
                throw new ContactRecipientInvalidException();
            }
            addRecipient(request, recipient);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates recipient of request by uuid.
     * @param requestUUID the request uuid
     * @param recipientUUID the recipient mail
     * @param recipient the recipient
     * @throws ContactRecipientInvalidException if recipient is invalid
     * @throws ContactRecipientAlreadyExistsException if recipient with given mail already exists
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     * @throws ContactRecipientOriginException if request has wrong origin
     */
    public void updateRecipientByUUID(UUID requestUUID, UUID recipientUUID, ContactRecipient recipient)
            throws ContactRequestNotFoundException, ContactRecipientOriginException, ContactRecipientNotFoundException,
            ContactRequestStateException, ContactRecipientAlreadyExistsException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!isWarmState(request.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipient outdated = recipientDAO.findByUUID(recipientUUID);
            if (!ContactRecipientOrigin.MANUAL.equals(outdated.getOrigin())
                      && (!Objects.equals(outdated.getName(), recipient.getName())
                      || !Objects.equals(outdated.getOrigin(), recipient.getOrigin())
                      || !Objects.equals(outdated.getMail(), recipient.getMail()))) {
                throw new ContactRecipientOriginException();
            }
            recipient.setRequest(outdated.getRequest());
            recipient.setId(outdated.getId());
            recipient.setFailed(null); // sanitizing
            recipient.setSent(null);
            update(recipient);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates recipient of request by given mail.
     * @param requestUUID the request uuid
     * @param mail the recipient mail
     * @param recipient the recipient
     * @throws ContactRecipientInvalidException if recipient is invalid
     * @throws ContactRecipientAlreadyExistsException if recipient with given mail already exists
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     * @throws ContactRecipientOriginException if request has wrong origin
     */
    public void updateRecipientByMail(UUID requestUUID, String mail, ContactRecipient recipient)
            throws ContactRequestNotFoundException, ContactRecipientOriginException, ContactRecipientNotFoundException,
            ContactRequestStateException, ContactRecipientAlreadyExistsException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!isWarmState(request.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipient outdated = request.getRecipients().stream().filter(r -> r.getMail().equals(mail))
                    .findFirst().orElseThrow(() -> new ContactRecipientNotFoundException());
            if (!ContactRecipientOrigin.MANUAL.equals(outdated.getOrigin())
                      && (!Objects.equals(outdated.getName(), recipient.getName())
                      || !Objects.equals(outdated.getOrigin(), recipient.getOrigin())
                      || !Objects.equals(outdated.getMail(), recipient.getMail()))) {
                throw new ContactRecipientOriginException();
            }
            recipient.setRequest(outdated.getRequest());
            recipient.setId(outdated.getId());
            recipient.setFailed(null); // sanitizing
            recipient.setSent(null);
            update(recipient);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates recipient.
     * @param recipient the recipient with parent and id
     * @throws ContactRecipientAlreadyExistsException if recipient with given mail already exists
     * @throws ContactRecipientNotFoundException if recipient cannot be found
     */
    public void updateRecipient(ContactRecipient recipient) throws ContactRecipientNotFoundException,
            ContactRecipientAlreadyExistsException {
        try {
            writeLock.lock();
            update(recipient);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes recipient of request by given uuid.
     * @param requestUUID the request uuid
     * @param recipientUUID the recipient uuid
     * @throws ContactRequestNotFoundException if request cannot be found
     * @throws ContactRecipientNotFoundException if recipient cannot be found
     * @throws ContactRequestStateException if request is in wrong state
     * @throws ContactRecipientOriginException if request has wrong origin
     */
    public void removeRecipientByUUID(UUID requestUUID, UUID recipientUUID) throws ContactRequestNotFoundException,
            ContactRecipientOriginException, ContactRecipientNotFoundException, ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!isWarmState(request.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipient recipient = recipientDAO.findByUUID(recipientUUID);
            if (recipient == null) {
                new ContactRecipientNotFoundException();
            }
            if (!ContactRecipientOrigin.MANUAL.equals(recipient.getOrigin())) {
                throw new ContactRecipientOriginException();
            }
            removeRecipient(recipient);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Sets recipient as failed if to indicate message bounce.
     * @param requestUUID the request uuid
     * @param mail the recipient mail
     * @param failed failed
     * @throws ContactRequestNotFoundException if request does not exist
     * @throws ContactRecipientNotFoundException if recipient does not exist
    */
    public void setRecipientFailed(UUID requestUUID, String mail, boolean failed) throws ContactRequestNotFoundException,
            ContactRecipientNotFoundException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            final ContactRecipient recipient = request.getRecipients().stream().filter(r -> r.getMail().equals(mail))
                    .findFirst().orElseThrow(() -> new ContactRecipientNotFoundException());
            recipient.setFailed(new Date());
            recipientDAO.update(recipient);
            update(request); // update modified
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Adds recipient to given request.
     * @param request the request
     * @param recipient the recipient
     * @throws ContactRecipientAlreadyExistsException if recipient with mail already exists
     */
    private void addRecipient(ContactRequest request, ContactRecipient recipient)
            throws ContactRecipientAlreadyExistsException {
        if (checkRecipientExists(request.getRecipients(), recipient)) {
            throw new ContactRecipientAlreadyExistsException();
        }
        recipient.setRequest(request);
        recipientDAO.insert(recipient); // to get id
        update(request); // update modified
    }

    /**
     * Updates recipient.
     * @param recipient the recipient with parent and id
     * @throws ContactRecipientAlreadyExistsException if recipient with given mail already exists
     * @throws ContactRecipientNotFoundException if recipient cannot be found
     */
    private void update(ContactRecipient recipient) throws ContactRecipientNotFoundException,
            ContactRecipientAlreadyExistsException {
        if (!ContactValidator.getInstance().validateRecipient(recipient)) {
            throw new ContactRecipientInvalidException();
        }
        final ContactRecipient outdated = recipientDAO.findByID(recipient.getId());
        if (outdated == null) {
            throw new ContactRecipientNotFoundException();
        }
        if (!outdated.getMail().equals(recipient.getMail()) && checkRecipientExists(recipient.getRequest().getRecipients(), recipient)) {
            throw new ContactRecipientAlreadyExistsException();
        }
        outdated.setName(recipient.getName());
        outdated.setOrigin(recipient.getOrigin());
        outdated.setMail(recipient.getMail());
        outdated.setEnabled(recipient.isEnabled());
        if (recipient.getFailed() != null) {
            outdated.setFailed(recipient.getFailed());
        }
        if (recipient.getSent() != null) {
            outdated.setSent(recipient.getSent());
        }
        recipientDAO.update(outdated);
        update(outdated.getRequest()); // update modified
    }

    /**
     * Removes recipient.
     * @param recipient the recipient
     * @throws ContactRequestNotFoundException if parent request does not exist
     */
    private void removeRecipient(ContactRecipient recipient) throws ContactRequestNotFoundException {
        final ContactRequest request = recipient.getRequest();
        if (request == null) {
            throw new ContactRequestNotFoundException();
        }
        request.removeRecipient(recipient);
        update(request);
    }

    /**
     * Checks if given recipient list contains recipient.
     * @param recipients list of recipients
     * @param recipient the recipient
     * @return true if list contains recipient
     */
    private boolean checkRecipientExists(List<ContactRecipient> recipients, ContactRecipient recipient) {
        if (recipients.size() == 0) {
            return false;
        }
        return recipients.stream().filter(r -> r.getMail().equals(recipient.getMail())).findAny().isPresent();
    }

    /**
     * Checks if given state is warm.
     * @param state the state
     * @return true is state is warm
     */
    private boolean isWarmState(ContactRequestState state) {
        return (ContactRequestState.PROCESSED.equals(state) || ContactRequestState.RECEIVED.equals(state));
    }

    /**
     * Updates request and sets modified.
     * @param request the request
     */
    private void update(ContactRequest request) {
        request.setLastModified(new Date());
        request.setLastModifiedBy(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        requestDAO.update(request);
    }

    /**
     * Lazy instance holder
     */
    private static class Holder {
        static final ContactService INSTANCE = new ContactService();
    }
}
