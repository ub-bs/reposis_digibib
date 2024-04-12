package de.vzg.reposis.digibib.contact.exception;

/**
 * Exception if and email error occurs.
 */
public class ContactEmailException extends ContactException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs new exception with message and cause.
     *
     * @param message message
     * @param cause cause
     */
    public ContactEmailException(String message, Throwable cause) {
        super(message, "emailException", cause);
    }

}
