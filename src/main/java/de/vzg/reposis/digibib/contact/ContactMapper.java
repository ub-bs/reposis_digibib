package de.vzg.reposis.digibib.contact;

import java.util.Optional;

import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactPersonEvent;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.persistence.model.ContactPersonData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactPersonEventData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;

/**
 * Maps between domain and data.
 */
public class ContactMapper {

    /**
     * Maps and returns {@link ContactPersonData} to {@link ContactPerson}.
     *
     * @param recipientData recipient data.
     * @return contact person
     */
    public static ContactPerson toDomain(ContactPersonData recipientData) {
        final ContactPerson contactPerson
            = new ContactPerson(recipientData.getName(), recipientData.getMail(), recipientData.getOrigin());
        recipientData.getEvents().stream().map(ContactMapper::toDomain).forEach(contactPerson::addEvent);
        return contactPerson;
    }

    /**
     * Maps and returns {@link ContactPersonEventData} to {@link ContactPersonEvent}.
     *
     * @param data data
     * @return event
     */
    public static ContactPersonEvent toDomain(ContactPersonEventData data) {
        return new ContactPersonEvent(data.getDate(), data.getType());
    }

    /**
     * Maps and returns {@link ContactPerson} to {@link ContactPersonData}.
     *
     * @param contactPerson contact person
     * @return contact recipient data
     */
    public static ContactPersonData toData(ContactPerson contactPerson) {
        final ContactPersonData recipientData = new ContactPersonData();
        recipientData.setName(contactPerson.getName());
        recipientData.setMail(contactPerson.getMail());
        recipientData.setOrigin(contactPerson.getOrigin());
        contactPerson.getEvents().stream().map(ContactMapper::toData).forEach(recipientData::addEvent);
        return recipientData;
    }

    /**
     * Maps and returns {@link ContactPersonEvent} to {@link ContactPersonEventData}.
     *
     * @param personEvent event
     * @return event data
     */
    public static ContactPersonEventData toData(ContactPersonEvent personEvent) {
        final ContactPersonEventData data = new ContactPersonEventData();
        data.setDate(personEvent.date());
        data.setType(personEvent.type());
        return data;
    }

    /**
     * Maps and returns {@link ContactRequestData} to {@link ContactRequest}.
     *
     * @param requestData request data
     * @return contact request
     */
    public static ContactRequest toDomain(ContactRequestData requestData) {
        final ContactRequestBody requestBody = new ContactRequestBody(requestData.getName(), requestData.getFrom(),
            requestData.getOrcid(), requestData.getMessage());
        final ContactRequest request = new ContactRequest(requestBody);
        request.setId(requestData.getUuid());
        request.setObjectId(requestData.getObjectId());
        request.setComment(requestData.getComment());
        request.setCreated(requestData.getCreated());
        request.setCreatedBy(requestData.getCreatedBy());
        request.setDebugMesssage(requestData.getDebug());
        request.setLastModified(requestData.getLastModified());
        request.setLastModifiedBy(requestData.getLastModifiedBy());
        request.setState(requestData.getState());
        requestData.getPersons().stream().map(ContactMapper::toDomain).forEach(request.getContactPersons()::add);
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
        requestData.setDebug(request.getDebugMessage());
        requestData.setLastModified(request.getLastModified());
        requestData.setLastModifiedBy(request.getLastModifiedBy());
        requestData.setState(request.getState());
        requestData.setObjectId(request.getObjectId());
        request.getContactPersons().stream().map(ContactMapper::toData).forEach(requestData::addPerson);
        final ContactRequestBody requestBody = request.getBody();
        if (requestBody != null) {
            requestData.setFrom(requestBody.fromMail());
            requestData.setMessage(requestBody.message());
            requestData.setName(requestBody.fromName());
            Optional.ofNullable(requestBody.fromOrcid()).map(String::trim).ifPresent(requestData::setOrcid);
        }
        return requestData;
    }
}
