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

import de.vzg.reposis.digibib.contact.collect.ContactCollectorService;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.ContactAlreadyExistsException;
import de.vzg.reposis.digibib.contact.exception.ContactInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestInvalidException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.mail.ContactEmailService;
import de.vzg.reposis.digibib.contact.model.Contact;
import de.vzg.reposis.digibib.contact.model.ContactEvent;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.persistence.ContactRequestRepository;
import de.vzg.reposis.digibib.contact.persistence.ContactRequestRepositoryImpl;
import de.vzg.reposis.digibib.contact.persistence.model.ContactData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactEventData;
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

    private static final MCRJobQueue CONTACT_COLLECTOR_JOB_QUEUE
        = MCRJobQueue.getInstance(ContactCollectorJobAction.class);

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
            final ContactRequestData requestData = ContactMapper.toData(request);
            requestData.setCreated(new Date());
            requestData.setCreatedBy(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
            requestData.setStatus(ContactRequest.RequestStatus.RECEIVED);
            requestRepository.insert(requestData);
            request.setId(requestData.getUuid());
            CONTACT_COLLECTOR_JOB_QUEUE.add(ContactCollectorJobAction.createJob(requestData.getUuid()));
            ContactEmailService.sendConfirmationEmail(request);
            try {
                ContactEmailService.sendNewRequestEmail(request);
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
            if (!ContactValidator.getInstance().validateRequest(request)) {
                throw new ContactRequestInvalidException();
            }
            requestRepository.findByUuid(request.getId()).ifPresentOrElse(r -> {
                r.setStatus(request.getStatus());
                r.setComment(request.getComment());
                r.setEmail(request.getBody().email());
                r.setName(request.getBody().name());
                r.setMessage(request.getBody().message());
                r.setObjectId(request.getObjectId());
                r.setOrcid(request.getBody().orcid());
                final List<ContactData> newContactDatas = new ArrayList<>();
                for (Contact p : request.getContacts()) {
                    final Optional<Long> id
                        = r.getContacts().stream().filter(pe -> Objects.equals(pe.getEmail(), p.getEmail()))
                            .findAny().map(ContactData::getId);
                    if (id.isPresent()) {
                        final ContactData contactData = ContactMapper.toData(p);
                        contactData.setId(id.get());
                        newContactDatas.add(contactData);
                    }
                }
                r.getContacts().clear();
                newContactDatas.stream().forEach(r::addContact);
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
                if (r.getStatus().getValue() <= ContactRequest.RequestStatus.PROCESSED.getValue()) {
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
    public void addContactEvent(UUID requestId, String mail, ContactEvent event) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            requestData.getContacts().stream().filter(r -> Objects.equals(mail, r.getEmail())).findAny()
                .ifPresentOrElse(r -> r.addEvent(ContactMapper.toData(event)), () -> {
                    throw new ContactNotFoundException();
                });
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Contact getContactByEmail(UUID requestId, String mail) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            return requestData.getContacts().stream().filter(r -> Objects.equals(mail, r.getEmail())).findAny()
                .map(ContactMapper::toDomain).orElseThrow(() -> new ContactNotFoundException());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addContact(UUID requestId, Contact contact) {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateContact(contact)) {
                throw new ContactInvalidException();
            }
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!Objects.equals(ContactRequest.RequestStatus.PROCESSED, requestData.getStatus())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            if (checkContactExists(requestData.getContacts(), contact)) {
                throw new ContactAlreadyExistsException();
            }
            final ContactData contactData = ContactMapper.toData(contact);
            requestData.addContact(contactData);
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void deleteContactByEmail(UUID requestId, String mail) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!Objects.equals(ContactRequest.RequestStatus.PROCESSED, requestData.getStatus())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactData contactData
                = requestData.getContacts().stream().filter(r -> Objects.equals(mail, r.getEmail())).findAny()
                    .orElseThrow(() -> new ContactNotFoundException());
            requestData.removeContact(contactData);
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void updateContact(UUID requestId, Contact contact) {
        try {
            writeLock.lock();
            if (!ContactValidator.getInstance().validateContact(contact)) {
                throw new ContactInvalidException();
            }
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            if (!Objects.equals(ContactRequest.RequestStatus.PROCESSED, requestData.getStatus())) {
                throw new ContactRequestStateException("Not in warm state");
            }
            final ContactData outdated
                = requestData.getContacts().stream().filter(r -> Objects.equals(contact.getEmail(), r.getEmail()))
                    .findAny().orElseThrow(() -> new ContactNotFoundException());
            outdated.setName(contact.getName());
            outdated.setEmail(contact.getEmail());
            outdated.setReference(contact.getReference());
            outdated.setOrigin(contact.getOrigin());
            outdated.getEvents().clear();
            contact.getEvents().stream().map(ContactMapper::toData).forEach(outdated::addEvent);
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    private boolean checkContactExists(List<ContactData> contacts, Contact contact) {
        if (contacts.size() == 0) {
            return false;
        }
        return contacts.stream().filter(r -> Objects.equals(r.getEmail(), contact.getEmail())).findAny().isPresent();
    }

    private static class Holder {
        static final ContactServiceImpl INSTANCE = new ContactServiceImpl();
    }

    @Override
    public void collectContacts(UUID requestId) {
        try {
            writeLock.lock();
            final ContactRequestData requestData
                = requestRepository.findByUuid(requestId).orElseThrow(() -> new ContactRequestNotFoundException());
            final MCRObject object = MCRMetadataManager.retrieveMCRObject(requestData.getObjectId());
            final List<Contact> contacts = ContactCollectorService.collectContacts(object);
            if (contacts.isEmpty()) {
                addFallbackContact(contacts);
            }
            requestData.getContacts().clear();
            contacts.stream().map(ContactMapper::toData).forEach(requestData::addContact);
            requestData.setStatus(ContactRequest.RequestStatus.PROCESSED);
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    private void addFallbackContact(List<Contact> contacts) {
        final Contact fallback = new Contact(FALLBACK_NAME, FALLBACK_MAIL, ORIGIN_FALLBACK, null);
        contacts.add(fallback);
    }

    @Override
    public void forwardRequest(UUID requestId, String email) {
        try {
            writeLock.lock();
            final ContactRequestData requestData = requestRepository.findByUuid(requestId)
                .orElseThrow(() -> new ContactRequestNotFoundException());
            final ContactData contactData
                = requestData.getContacts().stream().filter(r -> Objects.equals(email, r.getEmail())).findAny()
                    .orElseThrow(() -> new ContactNotFoundException());
            try {
                contactData.addEvent(new ContactEventData(ContactEvent.EventType.SENT, new Date()));
                ContactEmailService.sendRequestEmail(ContactMapper.toDomain(requestData),
                    ContactMapper.toDomain(contactData));
            } catch (Exception e) {
                throw new MCRException("Sending failed");
            }
            requestRepository.save(requestData);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void handleBouncedMessages() {
        try {
            writeLock.lock();
            List<Message> reportMessages = null;
            try {
                reportMessages = ContactEmailService.fetchUnseenReportMessages("INBOX");
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
                                final Stream<String> emails
                                    = Arrays.asList(dsnMessage.getRecipients(Message.RecipientType.TO)).stream()
                                        .map(Address::toString);
                                final Date date = m.getReceivedDate();
                                processContactDsnMessageContent(emails, date, id);
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

    private void processContactDsnMessageContent(Stream<String> emails, Date date, UUID requestId) {
        final ContactRequestData requestData
            = requestRepository.findByUuid(requestId).orElseThrow(() -> new ContactRequestNotFoundException());
        emails.forEach(m -> {
            requestData.getContacts().stream().filter(r -> Objects.equals(m, r.getEmail())).findAny().ifPresent(r -> {
                final ContactEventData event = new ContactEventData();
                event.setDate(date);
                event.setType(ContactEvent.EventType.SENT_FAILED);
                r.addEvent(event);
            });
        });
        requestRepository.save(requestData);
    }

}
