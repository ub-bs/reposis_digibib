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

package de.vzg.reposis.digibib.contact.persistence.model;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * This class defines a model for a recipient.
 */
@Entity
@Table(name = "contactRecipient")
public class ContactRecipientData {

    /**
     * Internal id.
     */
    private long id;

    /**
     * Name of the recipient.
     */
    private String name;

    /**
     * Origin of recipient date.
     */
    private String origin;

    /**
     * Recipient mail.
     */
    private String mail;

    /**
     * Parent request of recipient.
     */
    private ContactRequestData request;

    /**
     * If the request is enabled to sending
     */
    private boolean enabled;

    /**
     * Date when the mail was bounced.
     */
    private Date failed;

    /**
     * Date when the mail was sent.
     */
    private Date sent;

    /**
     * Date when the recipient has confirmed.
     */
    private Date confirmed;

    public ContactRecipientData() {
    }

    /**
     * @return internal id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipientId", nullable = false)
    @JsonIgnore
    public long getId() {
        return id;
    }

    /**
     * @param id internal id
     */
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "type", nullable = false)
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Column(name = "mail", nullable = false)
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requestId")
    public ContactRequestData getRequest() {
        return request;
    }

    public void setRequest(ContactRequestData request) {
        this.request = request;
    }

    @Column(name = "enabled", nullable = false)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Column(name = "failed", nullable = true)
    public Date getFailed() {
        return failed;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    @Column(name = "sent", nullable = true)
    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    @Column(name = "confirmed", nullable = true)
    public Date getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Date confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(confirmed, enabled, failed, id, mail, name, origin, request, sent);
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
        ContactRecipientData other = (ContactRecipientData) obj;
        return Objects.equals(confirmed, other.confirmed) && enabled == other.enabled
            && Objects.equals(failed, other.failed) && id == other.id && Objects.equals(mail, other.mail)
            && Objects.equals(name, other.name) && origin == other.origin && Objects.equals(request, other.request)
            && Objects.equals(sent, other.sent);
    }
}
