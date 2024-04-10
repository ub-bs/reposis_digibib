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

import java.io.IOException;
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

import com.sun.mail.dsn.MultipartReport;

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
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactPersonEvent;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.persistence.ContactRequestRepository;
import de.vzg.reposis.digibib.contact.persistence.ContactRequestRepositoryImpl;
import de.vzg.reposis.digibib.contact.persistence.model.ContactPersonData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactPersonEventData;
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
            final String currentUserId = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
            requestData.setCreatedBy(currentUserId);
            requestData.setState(ContactRequest.RequestStatus.RECEIVED);
            requestRepository.insert(requestData);
            request.setId(requestData.getUuid());
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
                requestRepository.save(r);
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
                if (r.getState().getValue() <= ContactRequest.RequestStatus.PROCESSED.getValue()) {
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

    @Override
    public void addPersonEvent(UUID requestId, String mail, ContactPersonEvent event) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            requestData.getPersons().stream().filter(r -> Objects.equals(mail, r.getMail())).findAny()
                .ifPresentOrElse(r -> r.addEvent(ContactMapper.toData(event)), () -> {
                    throw new ContactPersonNotFoundException();
                });
            requestRepository.save(requestData);
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
            return requestData.getPersons().stream().filter(r -> Objects.equals(mail, r.getMail())).findAny()
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
            if (!Objects.equals(ContactRequest.RequestStatus.PROCESSED, requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            if (checkRecipientExists(requestData.getPersons(), contactPerson)) {
                throw new ContactPersonAlreadyExistsException();
            }
            final ContactPersonData recipientData = ContactMapper.toData(contactPerson);
            requestData.addPerson(recipientData);
            requestRepository.save(requestData);
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
            if (!Objects.equals(ContactRequest.RequestStatus.PROCESSED, requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactPersonData recipientData
                = requestData.getPersons().stream().filter(r -> Objects.equals(mail, r.getMail())).findAny()
                    .orElseThrow(() -> new ContactPersonNotFoundException());
            if (!Objects.equals(ORIGIN_MANUAL, recipientData.getOrigin())) {
                throw new ContactPersonOriginException();
            }
            requestData.removePerson(recipientData);
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    // TODO update events
    @Override
    public void updateContactPerson(UUID requestId, ContactPerson contactPerson) {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateContactPerson(contactPerson)) {
                throw new ContactPersonInvalidException();
            }
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!Objects.equals(ContactRequest.RequestStatus.PROCESSED, requestData.getState())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactPersonData outdated
                = requestData.getPersons().stream().filter(r -> Objects.equals(contactPerson.getMail(), r.getMail()))
                    .findAny().orElseThrow(() -> new ContactPersonNotFoundException());
            if (!Objects.equals(ORIGIN_MANUAL, outdated.getOrigin())
                && (!Objects.equals(outdated.getName(), contactPerson.getName())
                    || !Objects.equals(outdated.getOrigin(), contactPerson.getOrigin())
                    || !Objects.equals(outdated.getMail(), contactPerson.getMail()))) {
                throw new ContactPersonOriginException();
            }
            outdated.setName(contactPerson.getName());
            outdated.setOrigin(contactPerson.getOrigin());

            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    private boolean checkRecipientExists(List<ContactPersonData> recipients, ContactPerson contactPerson) {
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
            requestData.getPersons().clear();
            contactPersons.stream().map(ContactMapper::toData).forEach(requestData::addPerson);
            requestData.setState(ContactRequest.RequestStatus.PROCESSED);
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    private void addFallbackRecipient(List<ContactPerson> recipients) {
        final ContactPerson fallback = new ContactPerson(FALLBACK_NAME, FALLBACK_MAIL, ORIGIN_FALLBACK, null);
        recipients.add(fallback);
    }

    @Override
    public void forwardRequest(UUID requestId, String mail) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            final ContactPersonData contactPerson
                = requestData.getPersons().stream().filter(r -> Objects.equals(mail, r.getMail()))
                    .findAny().orElseThrow(() -> new ContactPersonNotFoundException());
            doForwardRequest(requestData, contactPerson);
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    private void doForwardRequest(ContactRequestData requestData, ContactPersonData contactRecipientData) {
        final ContactRequest request = ContactMapper.toDomain(requestData);
        try {
            contactRecipientData.addEvent(new ContactPersonEventData(new Date(), ContactPersonEvent.EventType.SENT));
            ContactMailService.sendRequestMailToRecipient(request, ContactMapper.toDomain(contactRecipientData));
        } catch (Exception e) {
            throw new MCRException("Sending failed");
        }
    }

    @Override
    public void handleBouncedMessages() {
        try {
            writeLock.lock();
            List<Message> reportMessages = null;
            try {
                reportMessages = ContactMailService.fetchUnseenReportMessages("INBOX");
            } catch (MessagingException e) {
                throw new ContactException("Error while fetching report messages");
            }
            reportMessages.stream().forEach(m -> {
                try {
                    MultipartReport report = (MultipartReport) ((MimeMessage) m).getContent();
                    final MimeMessage dsnMessage = report.getReturnedMessage();
                    if (dsnMessage == null) {
                        return;
                    }
                    Optional.ofNullable(dsnMessage.getHeader(ContactConstants.REQUEST_HEADER_NAME, null))
                        .map(UUID::fromString).ifPresent(id -> {
                            try {
                                final Stream<String> mails
                                    = Arrays.asList(dsnMessage.getRecipients(Message.RecipientType.TO)).stream()
                                        .map(Address::toString);
                                final Date date = m.getReceivedDate();
                                processContactDsnMessageContent(mails, date, id);
                            } catch (MessagingException | ContactRequestNotFoundException e) {
                                LOGGER.error("Error while processing dsn message for id: {}", id, e);
                            } finally {
                                try {
                                    m.setFlag(Flags.Flag.SEEN, true);
                                } catch (MessagingException e) {
                                    LOGGER.error("Error while flagging message for {} as seen", id, e);
                                }
                            }
                        });
                } catch (IOException | MessagingException e) {
                    LOGGER.error("Error while proccessing report message");
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
            requestData.getPersons().stream().filter(r -> Objects.equals(m, r.getMail())).findAny().ifPresent(r -> {
                final ContactPersonEventData event = new ContactPersonEventData();
                event.setDate(date);
                event.setType(ContactPersonEvent.EventType.SENT_FAILED);
                r.addEvent(event);
            });
        });
        requestRepository.save(requestData);
    }

}
