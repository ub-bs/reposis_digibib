package de.vzg.reposis.digibib.contact.validation;

import de.vzg.reposis.digibib.contact.model.ContactRequest;

import org.apache.commons.validator.routines.EmailValidator;

public class ValidationHelper {

    public static boolean validateEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public static boolean validateContactRequest(ContactRequest contactRequest) {
        final String sender = contactRequest.getSender();
        if (sender == null || !validateEmail(sender)) {
            return false;
        }
        final String name = contactRequest.getName();
        if (name == null || name.isEmpty()) {
            return false;
        }
        final String message = contactRequest.getMessage();
        if (message == null || message.isEmpty()) {
            return false;
        }
        return true;
    }
}
