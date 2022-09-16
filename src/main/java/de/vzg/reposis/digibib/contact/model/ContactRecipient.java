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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NamedQueries({
    @NamedQuery(name = "ContactRecipient.findByUUID",
        query = "SELECT r"
            + "  FROM ContactRecipient r"
            + "  WHERE r.UUID = :uuid"),
})

/**
 * This class defines a model for a recipient.
 */
@Entity
@Table(name = "contact_recipient")
public class ContactRecipient {

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
    private ContactRecipientOrigin origin;

    /**
     * Recipient mail.
     */
    private String email;

    /**
     * Parent request of recipient.
     */
    private ContactRequest request;

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
     * Date when the recipeint has confirmed.
     */
    private Date confirmed;

    /**
     * Uuid of recipient.
     */
    @Column(name = "uuid", unique = true, updatable = false, nullable = false, columnDefinition = "binary(16)")
    private UUID uuid;

    public ContactRecipient() { }

    public ContactRecipient(String name, ContactRecipientOrigin origin, String email) {
        this.name = name;
        this.origin = origin;
        this.email = email;
        enabled = true;
    }

    public ContactRecipient(ContactRecipientOrigin origin, String email) {
        this.origin = origin;
        this.email = email;
        enabled = true;
    }

    /**
     * @return internal id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id",
        nullable = false)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type",
        nullable = false)
    public ContactRecipientOrigin getOrigin(){
        return origin;
    }

    public void setOrigin(ContactRecipientOrigin origin) {
        this.origin = origin;
    }

    @Column(name = "email",
        nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_request_id")
    public ContactRequest getRequest() {
        return request;
    }

    public void setRequest(ContactRequest request) {
        this.request = request;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    protected void prepersistUUIDModel() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    @Column(name = "enabled",
        nullable = false)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Column(name = "failed",
        nullable = true)
    public Date getFailed() {
        return failed;
    }

    public void setFailed(Date failed) {
        this.failed = failed;
    }

    @Column(name = "sent",
        nullable = true)
    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    @Column(name = "confirmed",
        nullable = true)
    public Date getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Date confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + email.hashCode();
        hash = 31 * hash + request.hashCode();
        return hash;
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
        ContactRecipient other = (ContactRecipient) obj;
        return Objects.equals(request, other.getRequest())
                && Objects.equals(email, other.getEmail());
    }

    @Override
    public String toString() {
        return String.format("ID: %d, \nEmail: %s", id, email);
    }
}
