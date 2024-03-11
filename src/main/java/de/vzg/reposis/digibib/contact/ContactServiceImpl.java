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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import de.vzg.reposis.digibib.contact.exception.ContactRecipientAlreadyExistsException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientOriginException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientStateException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.persistence.ContactRecipientRepository;
import de.vzg.reposis.digibib.contact.persistence.ContactRecipientRepositoryImpl;
import de.vzg.reposis.digibib.contact.persistence.ContactRequestRepository;
import de.vzg.reposis.digibib.contact.persistence.ContactRequestRepositoryImpl;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRecipientData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;
import de.vzg.reposis.digibib.contact.validation.ContactValidator;

public class ContactServiceImpl implements ContactService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Lock readLock;

    private final Lock writeLock;

    private final ContactRequestRepository requestRepository;

    private final ContactRecipientRepository recipientRepository;

    private ContactServiceImpl() {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        requestRepository = new ContactRequestRepositoryImpl();
        recipientRepository = new ContactRecipientRepositoryImpl();
    }

    public static ContactServiceImpl getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public ContactRequest getRequest(UUID requestId) {
        try {
            readLock.lock();
            return requestRepository.findByUUID(requestId).map(ContactMapper::toDomain)
                .orElseThrow(() -> new ContactRequestNotFoundException());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<ContactRequest> listAllRequests() {
        try {
            readLock.lock();
            return requestRepository.findAll().stream().map(ContactMapper::toDomain).toList();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void addRequest(ContactRequest request) {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateRequest(request)) {
                throw new ContactRequestInvalidException();
            }
            final MCRObjectID objectID = request.getObjectId();
            if (objectID == null || !MCRMetadataManager.exists(objectID)) {
                throw new ContactRequestInvalidException(objectID.toString() + " does not exist.");
            }
            request.setRecipients(new ArrayList<ContactRecipient>()); // sanitize recipients
            final ContactRequestData requestData = ContactMapper.toData(request);
            final Date currentDate = new Date();
            requestData.setCreated(currentDate);
            requestData.setLastModified(currentDate);
            final String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
            requestData.setCreatedBy(currentUserID);
            requestData.setLastModifiedBy(currentUserID);
            requestData.setState(ContactRequestState.RECEIVED);
            requestRepository.insert(requestData);

            ContactServiceHelper.sendConfirmationMail(request);
            try {
                ContactServiceHelper.sendNewRequestMail(request);
            } catch (Exception e) {
                // notification mail is not required, log is sufficient
                LOGGER.error("Cannot send notification mail", e);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void updateRequest(ContactRequest request) {
        try {
            writeLock.lock();
            requestRepository.findByUUID(request.getId()).ifPresentOrElse(r -> update(r), () -> {
                throw new ContactRequestNotFoundException();
            });
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void deleteRequest(UUID requestId) {
        try {
            writeLock.lock();
            requestRepository.findByUUID(requestId).ifPresentOrElse(r -> {
                if (r.getState().getValue() <= ContactRequestState.PROCESSED.getValue()) {
                    requestRepository.remove(r);
                } else {
                    throw new ContactRequestStateException("A forwarded request cannot be deleted.");
                }
            }, () -> {
                throw new ContactRequestNotFoundException();
            });
        } finally {
            writeLock.unlock();
        }
    }

    private void update(ContactRequestData request) {
        request.setLastModified(new Date());
        request.setLastModifiedBy(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        requestRepository.save(request);
    }

    @Override
    public void confirmRequest(UUID requestId, UUID recipientId) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUUID(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            requestData.getRecipients().stream().filter(r -> Objects.equals(r.getUUID(), recipientId)).findAny()
                .ifPresentOrElse(r -> r.setConfirmed(new Date()), () -> {
                    throw new ContactRecipientNotFoundException();
                });
            requestData.setState(ContactRequestState.CONFIRMED);
            update(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void forwardRequest(UUID requestId) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUUID(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!(Objects.equals(ContactRequestState.PROCESSED, requestData.getState())
                || Objects.equals(ContactRequestState.SENDING_FAILED, requestData.getState()))) {
                throw new ContactRequestStateException("Request is not ready.");
            }
            requestData.setState(ContactRequestState.SENDING);
            update(requestData);
            // TODO use MCRJob
            final ContactRequest request = ContactMapper.toDomain(requestData);
            Thread thread = new Thread(new ContactForwardRequestTask(request));
            thread.start();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void forwardRequest(UUID requestId, UUID recipientId) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUUID(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (requestData.getState().getValue() < ContactRequestState.SENT.getValue()) {
                throw new ContactRequestStateException("Request needs to be forwarded.");
            }
            final ContactRecipientData recipientData = requestData.getRecipients().stream()
                .filter(r -> Objects.equals(r.getUUID(), recipientId)).findAny()
                .orElseThrow(() -> new ContactRecipientNotFoundException());
            if (recipientData.getSent() != null && recipientData.getFailed() == null) {
                throw new ContactRecipientStateException("Recipient is in wrong state.");
            }
            try {
                final ContactRecipient recipient = ContactMapper.toDomain(recipientData);
                final ContactRequest request = ContactMapper.toDomain(requestData);
                ContactServiceHelper.sendRequestToRecipient(request, recipient);
                recipientData.setFailed(null);
                recipientData.setSent(new Date());
            } catch (Exception e) {
                LOGGER.error(e);
                recipientData.setFailed(new Date());
                throw new MCRException("Sending failed.");
            } finally {
                update(requestData);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public ContactRecipient getRecipient(UUID recipientId) {
        try {
            writeLock.lock();
            return recipientRepository.findByUUID(recipientId).map(ContactMapper::toDomain)
                .orElseThrow(() -> new ContactRecipientNotFoundException());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addRecipient(UUID requestId, ContactRecipient recipient) {
        try {
            writeLock.lock();
            if (!Objects.equals(ContactRecipientOrigin.MANUAL, recipient.getOrigin())) {
                throw new ContactRecipientOriginException();
            }
            if (!ContactValidator.getInstance().validateRecipient(recipient)) {
                throw new ContactRecipientInvalidException();
            }
            final ContactRequestData requestData = requestRepository.findByUUID(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!checkWarmState(requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            if (checkRecipientExists(requestData.getRecipients(), recipient)) {
                throw new ContactRecipientAlreadyExistsException();
            }
            final ContactRecipientData recipientData = ContactMapper.toData(recipient);
            recipientData.setRequest(requestData);
            requestData.addRecipient(recipientData);
            update(requestData); // update modified
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void deleteRecipient(UUID requestId, UUID recipientId) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUUID(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!checkWarmState(requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipientData recipientData = recipientRepository.findByUUID(recipientId)
                .orElseThrow(() -> new ContactRecipientNotFoundException());
            if (!Objects.equals(ContactRecipientOrigin.MANUAL, recipientData.getOrigin())) {
                throw new ContactRecipientOriginException();
            }
            requestData.removeRecipient(recipientData);
            update(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void updateRecipient(UUID requestId, ContactRecipient recipient) {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateRecipient(recipient)) {
                throw new ContactRecipientInvalidException();
            }
            final ContactRequestData requestData = requestRepository.findByUUID(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!checkWarmState(requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipientData outdated = recipientRepository.findByUUID(recipient.getId())
                .orElseThrow(() -> new ContactRecipientNotFoundException());
            if (!Objects.equals(ContactRecipientOrigin.MANUAL, outdated.getOrigin())
                && (!Objects.equals(outdated.getName(), recipient.getName())
                    || !Objects.equals(outdated.getOrigin(), recipient.getOrigin())
                    || !Objects.equals(outdated.getMail(), recipient.getMail()))) {
                throw new ContactRecipientOriginException();
            }
            if (!Objects.equals(outdated.getMail(), recipient.getMail())
                && checkRecipientExists(requestData.getRecipients(), recipient)) {
                throw new ContactRecipientAlreadyExistsException();
            }
            outdated.setName(recipient.getName());
            outdated.setOrigin(recipient.getOrigin());
            outdated.setMail(recipient.getMail());
            outdated.setEnabled(recipient.isEnabled());
            Optional.ofNullable(recipient.getFailed()).ifPresent(outdated::setFailed);
            Optional.ofNullable(recipient.getSent()).ifPresent(outdated::setSent);
            recipientRepository.save(outdated);
            update(outdated.getRequest()); // update modified
        } finally {
            writeLock.unlock();
        }
    }

    private boolean checkRecipientExists(List<ContactRecipientData> recipients, ContactRecipient recipient) {
        if (recipients.size() == 0) {
            return false;
        }
        return recipients.stream().filter(r -> Objects.equals(r.getMail(), recipient.getMail())).findAny().isPresent();
    }

    private boolean checkWarmState(ContactRequestState state) {
        return (Objects.equals(ContactRequestState.PROCESSED, state)
            || Objects.equals(ContactRequestState.RECEIVED, state));
    }

    private static class Holder {
        static final ContactServiceImpl INSTANCE = new ContactServiceImpl();
    }

}
