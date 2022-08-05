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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.vzg.reposis.digibib.contact.dao.ContactRequestDAO;
import de.vzg.reposis.digibib.contact.dao.ContactRequestDAOImpl;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.validation.ContactValidationHelper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;

public class ContactRequestService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String FALLBACK_EMAIL = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackEmail");

    private final ReadWriteLock lock;

    private final Lock readLock;

    private final Lock writeLock;

    private final ContactRequestDAO contactRequestDAO;

    private ContactRequestService() {
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        contactRequestDAO = new ContactRequestDAOImpl();
    }

    public static ContactRequestService getInstance() {
        return Holder.INSTANCE;
    }

    public List<ContactRequest> getContactRequests() {
        try {
            readLock.lock();
            return List.copyOf(contactRequestDAO.findAll());
        } finally {
            readLock.unlock();
        }
    }

    public ContactRequest getContactRequestByID(long id) {
        try {
            readLock.lock();
            return contactRequestDAO.findByID(id);
        } finally {
            readLock.unlock();
        }
    }

    public List<ContactRequest> listContactRequestsByState(ContactRequestState state) {
        try {
            readLock.lock();
            return new ArrayList(contactRequestDAO.findByState(state));
        } finally {
            readLock.unlock();
        }
    }

    public void insertContactRequest(ContactRequest contactRequest) throws ContactRequestInvalidException, MCRException {
        try {
            writeLock.lock();
            if (!ContactValidationHelper.validateContactRequest(contactRequest)) {
                throw new ContactRequestInvalidException();
            }
            final MCRObjectID objectID = contactRequest.getObjectID();
            if (objectID == null || !MCRMetadataManager.exists(objectID)) {
                throw new MCRException(objectID.toString() + " does not exist.");
            }
            final Date currentDate = new Date();
            contactRequest.setCreated(currentDate);
            contactRequest.setLastModified(currentDate);
            final String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
            contactRequest.setRecipients(new ArrayList()); // sanitize recipients
            contactRequest.setCreatedBy(currentUserID);
            contactRequest.setLastModifiedBy(currentUserID);
            contactRequest.setState(ContactRequestState.RECEIVED);
            contactRequestDAO.insert(contactRequest);
        } finally {
            writeLock.unlock();
        }
    }

    public void updateContactRequest(ContactRequest contactRequest) throws ContactRequestNotFoundException {
        try {
            writeLock.lock();
            final long id = contactRequest.getId();
            if (contactRequestDAO.findByID(id) != null) {
                update(contactRequest);
            } else {
                throw new ContactRequestNotFoundException();
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removeContactRequestByID(long id) throws ContactRequestNotFoundException,
            ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest contactRequest = contactRequestDAO.findByID(id);
            if (contactRequest == null) {
                throw new ContactRequestNotFoundException();
            }
            contactRequestDAO.remove(contactRequest);
        } finally {
            writeLock.unlock();
        }
    }

    public void rejectContactRequest(long id) throws ContactRequestNotFoundException,
            ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest contactRequest = contactRequestDAO.findByID(id);
            if (contactRequest == null) {
                throw new ContactRequestNotFoundException();
            }
            final ContactRequestState state = contactRequest.getState();
            if (!(ContactRequestState.RECEIVED.equals(state) || ContactRequestState.PROCESSED.equals(state))) {
                throw new ContactRequestStateException("Contact request state is not ready.");
            }
            contactRequest.setState(ContactRequestState.REJECTED);
            update(contactRequest);
        } finally {
            writeLock.unlock();
        }
    }

    public void forwardContactRequest(long id) throws ContactRequestNotFoundException,
            ContactRequestStateException {
        try {
            writeLock.lock();
            final ContactRequest contactRequest = contactRequestDAO.findByID(id);
            if (contactRequest == null) {
                throw new ContactRequestNotFoundException();
            }
            if (!ContactRequestState.PROCESSED.equals(contactRequest.getState())) {
                throw new ContactRequestStateException("Contact request state is not ready.");
            }
            contactRequest.setState(ContactRequestState.ACCEPTED);
            update(contactRequest);
            new Thread(new ContactSendTask(contactRequest)).run(); // TODO necessary?
        } finally {
            writeLock.unlock();
        }
    }

    private void update(ContactRequest contactRequest) {
        contactRequest.setLastModified(new Date());
        // contactRequest.setLastModifiedBy(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        contactRequestDAO.update(contactRequest);
    }

    private static class Holder {
        static final ContactRequestService INSTANCE = new ContactRequestService();
    }
}
