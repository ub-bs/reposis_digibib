package de.vzg.reposis.digibib.contact.persistence.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Defines contact request body model.
 */
@Embeddable
public class ContactRequestBodyData {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "orcid", nullable = true)
    private String orcid;

    public ContactRequestBodyData() {

    }

    public ContactRequestBodyData(String name, String email, String message, String orcid) {
        setName(name);
        setEmail(email);
        setMessage(message);
        setOrcid(orcid);
    }

    /**
     * Returns from email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets from email.
     *
     * @param from email
     */
    public void setEmail(String from) {
        this.email = from;
    }

    /**
     * Returns from message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets from message.
     *
     * @param message message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns from name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets from name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns from orcid.
     *
     * @return orcid
     */
    public String getOrcid() {
        return orcid;
    }

    /**
     * Sets from orcid.
     *
     * @param orcid orcid
     */
    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, message, name, orcid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ContactRequestBodyData other = (ContactRequestBodyData) obj;
        return Objects.equals(email, other.email) && Objects.equals(message, other.message)
            && Objects.equals(name, other.name) && Objects.equals(orcid, other.orcid);
    }
}
