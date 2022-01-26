package de.vzg.reposis.digibib.contact.exception;

import org.mycore.common.MCRException;

public class ContactException extends MCRException {

    private String errorCode;

    public ContactException(String message) {
        super(message);
        errorCode = "contactError";
    }

    public ContactException(Throwable cause) {
        super(cause);
        errorCode = "contactError";
    }

    public ContactException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
