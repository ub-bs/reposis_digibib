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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.services.queuedjob.MCRJobQueue;

import de.vzg.reposis.digibib.contact.collect.ContactPersonCollectorService;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonAlreadyExistsException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactPersonOriginException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.mail.ContactMailService;
import de.vzg.reposis.digibib.contact.model.ContactForwarding;
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.persistence.ContactRequestRepository;
import de.vzg.reposis.digibib.contact.persistence.ContactRequestRepositoryImpl;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRecipientData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;
import de.vzg.reposis.digibib.contact.validation.ContactValidator;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class ContactServiceImpl implements ContactService {

    private static final String FALLBACK_MAIL = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Mail");

    private static final String FALLBACK_NAME = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Name");

    private static final Logger LOGGER = LogManager.getLogger();

    private static final MCRJobQueue PERSON_COLLECTOR_JOB_QUEUE
        = MCRJobQueue.getInstance(ContactPersonCollectorJobAction.class);

    private static final MCRJobQueue FORWARDING_JOB_QUEUE
        = MCRJobQueue.getInstance(ContactRequestForwardJobAction.class);

    private static final String ORIGIN_MANUAL = "manual";

    private static final String ORIGIN_FALLBACK = "fallback";

    private final Lock readLock;

    private final Lock writeLock;

    private final ContactRequestRepository requestRepository;

    private ContactServiceImpl() {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        requestRepository = new ContactRequestRepositoryImpl();
    }

    public static ContactServiceImpl getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public ContactRequest getRequest(UUID requestId) {
        try {
            readLock.lock();
            return requestRepository.findByUuid(requestId).map(ContactMapper::toDomain)
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
    public void createRequest(MCRObjectID objectId, ContactRequestBody requestBody) {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateRequestBody(requestBody)) {
                throw new ContactRequestInvalidException();
            }
            if (!MCRMetadataManager.exists(objectId)) {
                throw new ContactRequestInvalidException(objectId + " does not exist.");
            }
            final ContactRequest request = new ContactRequest(requestBody);
            request.setObjectId(objectId);
            request.setContactPersons(new ArrayList<ContactPerson>()); // sanitize recipients
            final ContactRequestData requestData = ContactMapper.toData(request);
            final Date currentDate = new Date();
            requestData.setCreated(currentDate);
            requestData.setLastModified(currentDate);
            final String currentUserId = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
            requestData.setCreatedBy(currentUserId);
            requestData.setLastModifiedBy(currentUserId);
            requestData.setState(ContactRequest.State.RECEIVED);
            requestRepository.insert(requestData);
            PERSON_COLLECTOR_JOB_QUEUE.add(ContactPersonCollectorJobAction.createJob(requestData.getUuid()));
            ContactMailService.sendConfirmationMail(request);
            try {
                ContactMailService.sendNewRequestMail(request);
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
            requestRepository.findByUuid(request.getId()).ifPresentOrElse(r -> {
                Optional.ofNullable(request.getComment()).ifPresent(r::setComment);
                update(r);
            }, () -> {
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
            requestRepository.findByUuid(requestId).ifPresentOrElse(r -> {
                if (r.getState().getValue() <= ContactRequest.State.PROCESSED.getValue()) {
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
    public void confirmForwarding(UUID requestId, String mail) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            requestData.getRecipients().stream().filter(r -> Objects.equals(mail, r.getMail())).findAny()
                .ifPresentOrElse(r -> r.setConfirmed(new Date()), () -> {
                    throw new ContactPersonNotFoundException();
                });
            requestData.setState(ContactRequest.State.CONFIRMED);
            update(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public ContactPerson getContactPerson(UUID requestId, String mail) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            return requestData.getRecipients().stream().filter(r -> Objects.equals(mail, r.getMail())).findAny()
                .map(ContactMapper::toDomain).orElseThrow(() -> new ContactPersonNotFoundException());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addContactPerson(UUID requestId, ContactPerson contactPerson) {
        try {
            writeLock.lock();
            if (!Objects.equals(ORIGIN_MANUAL, contactPerson.getOrigin())) {
                throw new ContactPersonOriginException();
            }
            if (!ContactValidator.getInstance().validateContactPerson(contactPerson)) {
                throw new ContactPersonInvalidException();
            }
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!Objects.equals(ContactRequest.State.PROCESSED, requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            if (checkRecipientExists(requestData.getRecipients(), contactPerson)) {
                throw new ContactPersonAlreadyExistsException();
            }
            final ContactRecipientData recipientData = ContactMapper.toData(contactPerson);
            requestData.addRecipient(recipientData);
            update(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void deleteContactPerson(UUID requestId, String mail) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!Objects.equals(ContactRequest.State.PROCESSED, requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipientData recipientData
                = requestData.getRecipients().stream().filter(r -> Objects.equals(mail, r.getMail())).findAny()
                    .orElseThrow(() -> new ContactPersonNotFoundException());
            if (!Objects.equals(ORIGIN_MANUAL, recipientData.getOrigin())) {
                throw new ContactPersonOriginException();
            }
            requestData.removeRecipient(recipientData);
            update(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void updateContactPerson(UUID requestId, ContactPerson contactPerson) {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateContactPerson(contactPerson)) {
                throw new ContactPersonInvalidException();
            }
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!Objects.equals(ContactRequest.State.PROCESSED, requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactRecipientData outdated
                = requestData.getRecipients().stream().filter(r -> Objects.equals(contactPerson.getMail(), r.getMail()))
                    .findAny().orElseThrow(() -> new ContactPersonNotFoundException());
            if (!Objects.equals(ORIGIN_MANUAL, outdated.getOrigin())
                && (!Objects.equals(outdated.getName(), contactPerson.getName())
                    || !Objects.equals(outdated.getOrigin(), contactPerson.getOrigin())
                    || !Objects.equals(outdated.getMail(), contactPerson.getMail()))) {
                throw new ContactPersonOriginException();
            }
            outdated.setName(contactPerson.getName());
            outdated.setOrigin(contactPerson.getOrigin());
            outdated.setEnabled(contactPerson.isEnabled());
            final ContactForwarding forwarding = contactPerson.getForwarding();
            if (forwarding != null) {
                Optional.ofNullable(forwarding.getFailed()).ifPresent(outdated::setFailed);
                Optional.ofNullable(forwarding.getDate()).ifPresent(outdated::setSent);
                Optional.ofNullable(forwarding.getConfirmed()).ifPresent(outdated::setConfirmed);
            }
            update(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    private boolean checkRecipientExists(List<ContactRecipientData> recipients, ContactPerson contactPerson) {
        if (recipients.size() == 0) {
            return false;
        }
        return recipients.stream().filter(r -> Objects.equals(r.getMail(), contactPerson.getMail())).findAny()
            .isPresent();
    }

    private static class Holder {
        static final ContactServiceImpl INSTANCE = new ContactServiceImpl();
    }

    @Override
    public void collectContactPersons(UUID requestId) {
        try {
            writeLock.lock();
            final ContactRequestData requestData
                = requestRepository.findByUuid(requestId).orElseThrow(() -> new ContactRequestNotFoundException());
            final MCRObject object = MCRMetadataManager.retrieveMCRObject(requestData.getObjectId());
            final List<ContactPerson> contactPersons = ContactPersonCollectorService.collectContactPersons(object);
            if (contactPersons.isEmpty()) {
                addFallbackRecipient(contactPersons);
            }
            requestData.getRecipients().clear();
            contactPersons.stream().map(ContactMapper::toData).forEach(requestData::addRecipient);
            requestData.setState(ContactRequest.State.PROCESSED);
            update(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    private void addFallbackRecipient(List<ContactPerson> recipients) {
        final ContactPerson fallback = new ContactPerson(FALLBACK_NAME, FALLBACK_MAIL, ORIGIN_FALLBACK);
        recipients.add(fallback);
    }

    @Override
    public void forwardRequest(UUID requestId, String mail) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            final ContactRecipientData contactPerson
                = requestData.getRecipients().stream().filter(r -> Objects.equals(mail, r.getMail()))
                    .findAny().orElseThrow(() -> new ContactPersonNotFoundException());
            doForwardRequest(requestData, contactPerson);
            update(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void forwardRequest(UUID requestId) {
        try {
            writeLock.lock();
            final ContactRequestData requestData
                = requestRepository.findByUuid(requestId).orElseThrow(() -> new ContactRequestNotFoundException());
            if (requestData.getState().getValue() >= ContactRequest.State.FORWARDED.getValue()) {
                throw new ContactRequestStateException("Request was already forwared");
            }
            requestData.getRecipients().stream().filter(p -> checkForwardable(p)).forEach(contactPerson -> {
                try {
                    doForwardRequest(requestData, contactPerson);
                } catch (MCRException e) {
                    contactPerson.setFailed(new Date());
                }
            });
            requestData.setState(ContactRequest.State.FORWARDED);
            requestData.setForwarded(new Date());
            update(requestData);
            try {
                final ContactRequest request = ContactMapper.toDomain(requestData);
                ContactMailService.sendRequestForwardedMail(request);
            } catch (Exception e) {
                LOGGER.error("Cannot send forward confirmation.", e);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void doForwardRequest(ContactRequestData requestData, ContactRecipientData contactRecipientData) {
        final ContactRequest request = ContactMapper.toDomain(requestData);
        try {
            contactRecipientData.setSent(new Date());
            ContactMailService.sendRequestMailToRecipient(request, ContactMapper.toDomain(contactRecipientData));
        } catch (Exception e) {
            throw new MCRException("Sending failed");
        }
    }

    @Override
    public void createRequestForwardingJob(UUID requestId) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!Objects.equals(ContactRequest.State.PROCESSED, requestData.getState())) {
                throw new ContactRequestStateException("Request is not ready.");
            }
            requestData.setState(ContactRequest.State.FORWARDING);
            update(requestData);
            FORWARDING_JOB_QUEUE.add(ContactRequestForwardJobAction.createJob(requestId));
        } finally {
            writeLock.unlock();
        }
    }

    private static boolean checkForwardable(ContactRecipientData contactPerson) {
        return contactPerson.isEnabled() && (contactPerson.getSent() == null);
    }

    @Override
    public void handleBouncedMessages() {
        try {
            writeLock.lock();
            List<MimeMessage> dsnMessages = null;
            try {
                dsnMessages = ContactMailService.fetchUnseenDsnMessages("INBOX");
            } catch (MessagingException e) {
                throw new ContactException("Error while fetching dsns");
            }
            dsnMessages.stream().forEach(m -> {
                try {
                    Optional.ofNullable(m.getHeader(ContactConstants.REQUEST_HEADER_NAME, null)).map(UUID::fromString)
                        .ifPresent(id -> {
                            try {
                                final Stream<String> mails
                                    = Arrays.asList(m.getRecipients(Message.RecipientType.TO)).stream()
                                        .map(Address::toString);
                                final Date date = m.getReceivedDate();
                                processContactDsnMessageContent(mails, date, id);
                                m.setFlag(Flags.Flag.SEEN, true);
                            } catch (MessagingException | ContactRequestNotFoundException e) {
                                LOGGER.error("Error while processing dsn message", e);
                            }
                        });
                } catch (MessagingException e) {
                    LOGGER.error("Error while proccessing dsn message");
                }
            });
        } finally {
            writeLock.unlock();
        }
    }

    private void processContactDsnMessageContent(Stream<String> mails, Date date, UUID requestId) {
        final ContactRequestData requestData
            = requestRepository.findByUuid(requestId).orElseThrow(() -> new ContactRequestNotFoundException());
        mails.forEach(m -> {
            requestData.getRecipients().stream().filter(r -> Objects.equals(m, r.getMail())).findAny().ifPresent(r -> {
                r.setFailed(date);
            });
        });
        update(requestData);
    }

}
