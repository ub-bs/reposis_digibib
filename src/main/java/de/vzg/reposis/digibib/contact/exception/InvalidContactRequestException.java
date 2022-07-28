package de.vzg.reposis.digibib.contact.exception;

public class InvalidContactRequestException extends ContactException {

    public InvalidContactRequestException() {
        super("invalidContactRequest", "invalid contact request.");
    }
}
