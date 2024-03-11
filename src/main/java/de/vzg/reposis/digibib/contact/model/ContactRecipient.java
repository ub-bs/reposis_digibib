/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vzg.reposis.digibib.contact.model;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Contact recipient model.
 */
public class ContactRecipient {

    private UUID id;

    private String name;

    private ContactRecipientOrigin origin;

    private String mail;

    private boolean enabled;

    private Date failed;

    private Date sent;

    private Date confirmed;

    private ContactRequest request;

    public ContactRecipient() {
    }

    public ContactRecipient(String name, ContactRecipientOrigin origin, String mail) {
        this.name = name;
        this.origin = origin;
        this.mail = mail;
        enabled = true;
    }

    public ContactRecipient(ContactRecipientOrigin origin, String mail) {
        this.origin = origin;
        this.mail = mail;
        enabled = true;
    }

    /**
     * Returns id of recipient.
     *
     * @return id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets id of recipient
     *
     * @param id id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns name of recipient.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets namne of recipient.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns origin of recipient.
     *
     * @return origin
     */
    public ContactRecipientOrigin getOrigin() {
        return origin;
    }

    /**
     * Sets origin of recipient.
     *
     * @param origin origin
     */
    public void setOrigin(ContactRecipientOrigin origin) {
        this.origin = origin;
    }

    /**
     * Returns mail address of recipient.
     *
     * @return mail address
     */
    public String getMail() {
        return mail;
    }

    /**
     * Sets mail address of
     *
     * @param mail
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /*
    public ContactRequest getRequest() {
        return request;
    }
    
    public void setRequest(ContactRequest request) {
        this.request = request;
    }
    */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getFailed() {
        return failed;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public Date getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Date confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(confirmed, enabled, failed, mail, name, origin, request, sent, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContactRecipient other = (ContactRecipient) obj;
        return Objects.equals(confirmed, other.confirmed) && enabled == other.enabled
            && Objects.equals(failed, other.failed) && Objects.equals(mail, other.mail)
            && Objects.equals(name, other.name) && origin == other.origin && Objects.equals(request, other.request)
            && Objects.equals(sent, other.sent) && Objects.equals(id, other.id);
    }

}
