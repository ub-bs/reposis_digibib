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

public class ContactRequestService {

    private final Lock readLock;

    private final Lock writeLock;

    private final ContactRequestDAO requestDAO;

    private final ContactRecipientDAO recipientDAO;

    private ContactRequestService() {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        requestDAO = new ContactRequestDAOImpl();
        recipientDAO = new ContactRecipientDAOImpl();
    }

    public static ContactRequestService getInstance() {
        return Holder.INSTANCE;
    }

    public List<ContactRequest> listRequests() {
        try {
            readLock.lock();
            return List.copyOf(requestDAO.findAll());
        } finally {
            readLock.unlock();
        }
    }

    public List<ContactRequest> listRequestsByState(ContactRequestState state) {
        try {
            readLock.lock();
            return new ArrayList(requestDAO.findByState(state));
        } finally {
            readLock.unlock();
        }
    }

    public ContactRequest getRequestByID(long id) {
        try {
            readLock.lock();
            return requestDAO.findByID(id);
        } finally {
            readLock.unlock();
        }
    }

    public ContactRequest getRequestByUUID(UUID uuid) {
        try {
            readLock.lock();
            return requestDAO.findByUUID(uuid);
        } finally {
            readLock.unlock();
        }
    }

    public void insertRequest(ContactRequest request) throws ContactRequestInvalidException, MCRException {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateRequest(request)) {
                throw new ContactRequestInvalidException();
            }
            final MCRObjectID objectID = request.getObjectID();
            if (objectID == null || !MCRMetadataManager.exists(objectID)) {
                throw new MCRException(objectID.toString() + " does not exist.");
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

    public void updateRequest(ContactRequest request) throws ContactRequestNotFoundException {
        try {
            writeLock.lock();
            final long id = request.getId();
            if (requestDAO.findByID(id) != null) {
                update(request);
            } else {
                throw new ContactRequestNotFoundException();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removeRequestByID(long id) throws ContactRequestNotFoundException,
            ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByID(id);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            requestDAO.remove(request);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeRequestByUUID(UUID uuid) throws ContactRequestNotFoundException,
            ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(uuid);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            requestDAO.remove(request);
        } finally {
            writeLock.unlock();
        }
    }

    public void forwardRequest(UUID id) throws ContactRequestNotFoundException,
            ContactRequestStateException, MCRException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(id);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!(ContactRequestState.PROCESSED.equals(request.getState())
                    || ContactRequestState.SENDING_FAILED.equals(request.getState()))) {
                throw new ContactRequestStateException("Contact request state is not ready.");
            }
            request.setState(ContactRequestState.SENDING);
            updateRequest(request);
            Thread thread = new Thread(new ContactSendTask(request));
            thread.start();
        } finally {
            writeLock.unlock();
        }
    }

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

    public void updateRecipientByMail(UUID requestUUID, String mail, ContactRecipient recipient) throws ContactRequestNotFoundException,
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
            final ContactRecipient outdated = request.getRecipients().stream().filter(r -> r.getEmail().equals(mail))
                    .findFirst().orElseThrow(() -> new ContactRecipientNotFoundException());
            if (!ContactRecipientOrigin.MANUAL.equals(outdated.getOrigin())
                      && (!Objects.equals(outdated.getName(), recipient.getName())
                      || !Objects.equals(outdated.getOrigin(), recipient.getOrigin())
                      || !Objects.equals(outdated.getEmail(), recipient.getEmail()))) {
                throw new ContactRecipientOriginException();
            }
            recipient.setRequest(outdated.getRequest());
            recipient.setId(outdated.getId());
            updateRecipient(recipient);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeRecipientByMail(UUID requestUUID, String mail) throws ContactRequestNotFoundException,
            ContactRecipientOriginException, ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!isWarmState(request.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipient recipient = request.getRecipients().stream().filter(r -> mail.equals(r.getEmail()))
                    .findFirst().orElseThrow(() -> new ContactRecipientNotFoundException());
            if (!ContactRecipientOrigin.MANUAL.equals(recipient.getOrigin())) {
                throw new ContactRecipientOriginException();
            }
            removeRecipient(recipient);
        } finally {
            writeLock.unlock();
        }
    }

    public void setRecipientFailed(UUID requestUUID, String mail, boolean failed) throws ContactRequestNotFoundException,
            ContactRecipientNotFoundException {
        try {
            writeLock.lock();
            final ContactRequest request = requestDAO.findByUUID(requestUUID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            final ContactRecipient recipient = request.getRecipients().stream().filter(r -> r.getEmail().equals(mail))
                    .findFirst().orElseThrow(() -> new ContactRecipientNotFoundException());
            recipient.setFailed(failed);
            recipientDAO.update(recipient);
            update(request); // update modified
        } finally {
            writeLock.unlock();
        }
    }

    protected void addRecipient(ContactRequest request, ContactRecipient recipient) throws ContactRecipientAlreadyExistsException {
        if (checkRecipientExists(request.getRecipients(), recipient)) {
            throw new ContactRecipientAlreadyExistsException();
        }
        recipient.setRequest(request);
        recipientDAO.insert(recipient); // to get id
        update(request); // update modified
    }

    protected void updateRecipient(ContactRecipient recipient) throws ContactRecipientNotFoundException,
            ContactRecipientAlreadyExistsException {
        if (!ContactValidator.getInstance().validateRecipient(recipient)) {
            throw new ContactRecipientInvalidException();
        }
        final ContactRecipient outdated = recipientDAO.findByID(recipient.getId());
        if (outdated == null) {
            throw new ContactRecipientNotFoundException();
        }
        if (!outdated.getEmail().equals(recipient.getEmail()) && checkRecipientExists(recipient.getRequest().getRecipients(), recipient)) {
            throw new ContactRecipientAlreadyExistsException();
        }
        outdated.setName(recipient.getName());
        outdated.setOrigin(recipient.getOrigin());
        outdated.setEmail(recipient.getEmail());
        outdated.setEnabled(recipient.isEnabled());
        outdated.setFailed(recipient.isFailed());
        outdated.setSent(recipient.isSent());
        recipientDAO.update(outdated);
        update(outdated.getRequest()); // update modified
    }

    protected void removeRecipient(ContactRecipient recipient) throws ContactRequestNotFoundException,
            ContactRequestStateException {
        final ContactRequest request = recipient.getRequest();
        if (request == null) {
            throw new ContactRequestNotFoundException();
        }
        request.removeRecipient(recipient);
        update(request);
    }

    private boolean checkRecipientExists(List<ContactRecipient> recipients , ContactRecipient recipient) {
        if (recipients.size() == 0) {
            return false;
        }
        return recipients.stream().filter(r -> r.getEmail().equals(recipient.getEmail())).findAny().isPresent();
    }

    private boolean isWarmState(ContactRequestState state) {
        return (ContactRequestState.PROCESSED.equals(state) || ContactRequestState.RECEIVED.equals(state));
    }

    private void update(ContactRequest request) {
        request.setLastModified(new Date());
        request.setLastModifiedBy(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        requestDAO.update(request);
    }

    private static class Holder {
        static final ContactRequestService INSTANCE = new ContactRequestService();
    }
}
