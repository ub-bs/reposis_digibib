package de.vzg.reposis.digibib.contact;

import java.util.Optional;

import de.vzg.reposis.digibib.contact.model.ContactForwarding;
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRecipientData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;

/**
 * Maps between domain and data.
 */
public class ContactMapper {

    /**
     * Maps and returns {@link ContactRecipientData} to {@link ContactPerson}.
     *
     * @param recipientData recipient data.
     * @return contact person
     */
    public static ContactPerson toDomain(ContactRecipientData recipientData) {
        final ContactPerson contactPerson
            = new ContactPerson(recipientData.getName(), recipientData.getMail(), recipientData.getOrigin());
        contactPerson.setEnabled(recipientData.isEnabled());
        if (recipientData.getSent() != null || recipientData.getFailed() != null
            || recipientData.getConfirmed() != null) {
            final ContactForwarding forwarding = new ContactForwarding();
            forwarding.setConfirmed(recipientData.getConfirmed());
            forwarding.setFailed(recipientData.getFailed());
            forwarding.setDate(recipientData.getSent());
            contactPerson.setForwarding(forwarding);
        }
        return contactPerson;
    }

    /**
     * Maps and returns {@link ContactPerson} to {@link ContactRecipientData}.
     *
     * @param contactPerson contact person
     * @return contact recipient data
     */
    public static ContactRecipientData toData(ContactPerson contactPerson) {
        final ContactRecipientData recipientData = new ContactRecipientData();
        recipientData.setName(contactPerson.getName());
        recipientData.setMail(contactPerson.getMail());
        recipientData.setOrigin(contactPerson.getOrigin());
        recipientData.setEnabled(contactPerson.isEnabled());
        if (contactPerson.getForwarding() != null) {
            recipientData.setConfirmed(contactPerson.getForwarding().getConfirmed());
            recipientData.setFailed(contactPerson.getForwarding().getFailed());
            recipientData.setSent(contactPerson.getForwarding().getDate());
        }
        return recipientData;
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
        request.setForwarded(requestData.getForwarded());
        request.setLastModified(requestData.getLastModified());
        request.setLastModifiedBy(requestData.getLastModifiedBy());
        request.setState(requestData.getState());
        requestData.getRecipients().stream().map(ContactMapper::toDomain).forEach(request.getContactPersons()::add);
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
        requestData.setForwarded(request.getForwarded());
        requestData.setLastModified(request.getLastModified());
        requestData.setLastModifiedBy(request.getLastModifiedBy());
        requestData.setState(request.getState());
        requestData.setObjectId(request.getObjectId());
        request.getContactPersons().stream().map(ContactMapper::toData).forEach(requestData::addRecipient);
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
