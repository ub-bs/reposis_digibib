package de.vzg.reposis.digibib.contact.exception;

public class InvalidMessageException extends ContactException {

    public InvalidMessageException() {
        super("invalidMessage", "invalid message.");
    }
}
