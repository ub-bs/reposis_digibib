package de.vzg.reposis.digibib.contact.exception;

public class ContactRequestNotFoundException extends ContactException {

    public ContactRequestNotFoundException() {
        super("contactRequestNotFound", "contact request not found.");
    }
}
