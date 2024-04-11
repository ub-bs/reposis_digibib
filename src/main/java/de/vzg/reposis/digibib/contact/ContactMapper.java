package de.vzg.reposis.digibib.contact;

import java.util.Optional;

import de.vzg.reposis.digibib.contact.model.Contact;
import de.vzg.reposis.digibib.contact.model.ContactEvent;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.persistence.model.ContactData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactEventData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;

/**
 * Maps between domain and data.
 */
public class ContactMapper {

    /**
     * Maps and returns {@link ContactData} to {@link Contact}.
     *
     * @param recipientData recipient data.
     * @return contact person
     */
    public static Contact toDomain(ContactData recipientData) {
        final Contact contactPerson
            = new Contact(recipientData.getName(), recipientData.getEmail(), recipientData.getOrigin(),
                recipientData.getReference());
        recipientData.getEvents().stream().map(ContactMapper::toDomain).forEach(contactPerson::addEvent);
        return contactPerson;
    }

    /**
     * Maps and returns {@link ContactEventData} to {@link ContactEvent}.
     *
     * @param data data
     * @return event
     */
    public static ContactEvent toDomain(ContactEventData data) {
        return new ContactEvent(data.getType(), data.getDate(), data.getComment());
    }

    /**
     * Maps and returns {@link Contact} to {@link ContactData}.
     *
     * @param contact contact
     * @return contact data
     */
    public static ContactData toData(Contact contact) {
        final ContactData contactData = new ContactData();
        contactData.setName(contact.getName());
        contactData.setEmail(contact.getEmail());
        contactData.setOrigin(contact.getOrigin());
        contactData.setReference(contact.getReference());
        contact.getEvents().stream().map(ContactMapper::toData).forEach(contactData::addEvent);
        return contactData;
    }

    /**
     * Maps and returns {@link ContactEvent} to {@link ContactEventData}.
     *
     * @param contactEvent event
     * @return event data
     */
    public static ContactEventData toData(ContactEvent contactEvent) {
        final ContactEventData contactEventData = new ContactEventData();
        contactEventData.setDate(contactEvent.date());
        contactEventData.setType(contactEvent.type());
        contactEventData.setComment(contactEvent.comment());
        return contactEventData;
    }

    /**
     * Maps and returns {@link ContactRequestData} to {@link ContactRequest}.
     *
     * @param requestData request data
     * @return contact request
     */
    public static ContactRequest toDomain(ContactRequestData requestData) {
        final ContactRequestBody requestBody = new ContactRequestBody(requestData.getName(), requestData.getEmail(),
            requestData.getOrcid(), requestData.getMessage());
        final ContactRequest request = new ContactRequest(requestBody);
        request.setId(requestData.getUuid());
        request.setObjectId(requestData.getObjectId());
        request.setComment(requestData.getComment());
        request.setCreated(requestData.getCreated());
        request.setCreatedBy(requestData.getCreatedBy());
        request.setStatus(requestData.getStatus());
        requestData.getContacts().stream().map(ContactMapper::toDomain).forEach(request.getContacts()::add);
        return request;
    }

    /**
     * Maps and returns {@link ContactRequest} to {@link ContactRequestData}.
     *
     * @param request request
     * @return contact request data
     */
    public static ContactRequestData toData(ContactRequest request) {
        final ContactRequestData requestData = new ContactRequestData();
        requestData.setComment(request.getComment());
        requestData.setCreated(request.getCreated());
        requestData.setCreatedBy(request.getCreatedBy());
        requestData.setStatus(request.getStatus());
        requestData.setObjectId(request.getObjectId());
        request.getContacts().stream().map(ContactMapper::toData).forEach(requestData::addContact);
        final ContactRequestBody requestBody = request.getBody();
        if (requestBody != null) {
            requestData.setEmail(requestBody.email());
            requestData.setMessage(requestBody.message());
            requestData.setName(requestBody.name());
            Optional.ofNullable(requestBody.orcid()).map(String::trim).ifPresent(requestData::setOrcid);
        }
        return requestData;
    }
}
