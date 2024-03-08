package de.vzg.reposis.digibib.contact;

import java.util.Optional;

import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRecipientData;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;

public class ContactMapper {

    public static ContactRecipient toDomain(ContactRecipientData recipientData) {
        final ContactRecipient recipient = new ContactRecipient();
        recipient.setConfirmed(recipientData.getConfirmed());
        recipient.setEnabled(recipientData.isEnabled());
        recipient.setFailed(recipientData.getFailed());
        recipient.setMail(recipientData.getMail());
        recipient.setOrigin(recipientData.getOrigin());
        recipient.setSent(recipientData.getSent());
        recipient.setUUID(recipientData.getUUID());
        return recipient;
    }

    public static ContactRecipientData toData(ContactRecipient recipient) {
        final ContactRecipientData recipientData = new ContactRecipientData();
        recipientData.setConfirmed(recipient.getConfirmed());
        recipientData.setEnabled(recipient.isEnabled());
        recipientData.setFailed(recipient.getFailed());
        recipientData.setMail(recipient.getMail());
        recipientData.setOrigin(recipient.getOrigin());
        recipientData.setSent(recipient.getSent());
        recipientData.setUUID(recipient.getUUID());
        return recipientData;
    }

    public static ContactRequest toDomain(ContactRequestData requestData) {
        final ContactRequest request = new ContactRequest();
        request.setComment(requestData.getComment());
        request.setCreated(requestData.getCreated());
        request.setCreatedBy(request.getCreatedBy());
        request.setDebug(requestData.getDebug());
        request.setForwarded(requestData.getForwarded());
        request.setFrom(requestData.getFrom());
        request.setLastModified(requestData.getLastModified());
        request.setLastModifiedBy(requestData.getLastModifiedBy());
        request.setMessage(requestData.getMessage());
        request.setName(requestData.getName());
        request.setObjectID(requestData.getObjectID());
        request.setORCID(requestData.getORCID());
        request.setState(requestData.getState());
        requestData.getRecipients().stream().map(ContactMapper::toDomain).forEach(request::addRecipient);
        return request;
    }

    public static ContactRequestData toData(ContactRequest request) {
        final ContactRequestData requestData = new ContactRequestData();
        requestData.setComment(request.getComment());
        requestData.setCreated(request.getCreated());
        requestData.setCreatedBy(request.getCreatedBy());
        requestData.setDebug(request.getDebug());
        requestData.setForwarded(request.getForwarded());
        requestData.setFrom(request.getFrom());
        requestData.setLastModified(request.getLastModified());
        requestData.setLastModifiedBy(request.getLastModifiedBy());
        requestData.setMessage(request.getMessage());
        requestData.setName(request.getName());
        requestData.setObjectID(request.getObjectID());
        Optional.ofNullable(request.getORCID()).map(String::trim).ifPresent(requestData::setORCID);
        requestData.setState(request.getState());
        request.getRecipients().stream().map(ContactMapper::toData).forEach(requestData::addRecipient);
        return requestData;
    }
}
