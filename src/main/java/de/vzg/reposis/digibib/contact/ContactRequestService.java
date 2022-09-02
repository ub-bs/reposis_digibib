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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.vzg.reposis.digibib.contact.dao.ContactRecipientDAO;
import de.vzg.reposis.digibib.contact.dao.ContactRecipientDAOImpl;
import de.vzg.reposis.digibib.contact.dao.ContactRequestDAO;
import de.vzg.reposis.digibib.contact.dao.ContactRequestDAOImpl;
import de.vzg.reposis.digibib.contact.exception.ContactException;
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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRTransactionHelper;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ContactRequestService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ReadWriteLock lock;

    private final Lock readLock;

    private final Lock writeLock;

    private final ContactRequestDAO requestDAO;

    private final ContactRecipientDAO recipientDAO;

    private ContactRequestService() {
        lock = new ReentrantReadWriteLock();
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

    public List<ContactRecipient> listRecipientsByUUID(UUID uuid) throws ContactRequestNotFoundException {
        try {
            readLock.lock();
            return Optional.ofNullable(requestDAO.findByUUID(uuid))
                    .map(r -> r.getRecipients()).orElseThrow(() -> new ContactRequestNotFoundException());
        } finally {
            readLock.unlock();
        }
    }

    public ContactRecipient getRecipientByUUID(UUID uuid) {
        try {
            readLock.lock();
            return recipientDAO.findByUUID(uuid);
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
            MCRTransactionHelper.beginTransaction();
            boolean commitError = false;
            try {
                request.setState(ContactRequestState.SENDING);
                update(request);
                MCRTransactionHelper.commitTransaction();
            } catch (Exception commitExc) {
                commitError = true;
                try {
                    MCRTransactionHelper.rollbackTransaction();
                } catch (Exception rollbackExc) {
                    LOGGER.error("Error while rollbacking transaction.", rollbackExc);
                }
            }
            if (!commitError) {
                MCRTransactionHelper.beginTransaction();
                try {
                    new ContactSendTask(request).call();
                    request.setState(ContactRequestState.SENT);
                } catch (Exception e) {
                    request.setState(ContactRequestState.SENDING_FAILED);
                    request.setComment(e.toString());
                } finally {
                    try {
                        update(request);
                        MCRTransactionHelper.commitTransaction();
                    } catch (Exception commitExc) {
                        commitError = true;
                        try {
                            MCRTransactionHelper.rollbackTransaction();
                        } catch (Exception rollbackExc) {
                            LOGGER.error("Error while rollbacking transaction.", rollbackExc);
                        }
                    }
                }
            }
            if (commitError) {
                throw new MCRException("Error while commiting state");
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void addRecipient(long requestID, ContactRecipient recipient) throws ContactRecipientInvalidException,
            ContactRequestNotFoundException, ContactRequestStateException {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateRecipient(recipient)) {
                throw new ContactRecipientInvalidException();
            }
            final ContactRequest request = requestDAO.findByID(requestID);
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!isWarmState(request.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            recipient.setRequest(request);
            recipientDAO.insert(recipient); // to get id
            update(request); // update modified
        } finally {
            writeLock.unlock();
        }
    }

    public void removeRecipient(ContactRecipient recipient) throws ContactRequestNotFoundException,
            ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest request = recipient.getRequest();
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!isWarmState(request.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            request.removeRecipient(recipient);
            update(request);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateRecipient(ContactRecipient recipient) throws ContactRequestNotFoundException,
        ContactRecipientNotFoundException, ContactRequestStateException {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateRecipient(recipient)) {
                throw new ContactRecipientInvalidException();
            }
            final ContactRequest request = recipient.getRequest();
            if (request == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!isWarmState(request.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipient outdated = recipientDAO.findByID(recipient.getId());
            if (outdated == null) {
                throw new ContactRecipientNotFoundException();
            }
            outdated.setName(recipient.getName());
            outdated.setOrigin(recipient.getOrigin());
            outdated.setEmail(recipient.getEmail());
            outdated.setEnabled(recipient.isEnabled());
            recipientDAO.update(outdated);
            update(request); // update modified
        } finally {
            writeLock.unlock();
        }
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
