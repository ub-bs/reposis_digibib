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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.mycore.backend.jpa.MCRObjectIDConverter;
import org.mycore.datamodel.metadata.MCRObjectID;

@NamedQueries({
    @NamedQuery(name = "ContactRequest.findAll",
        query = "SELECT r"
            + "  FROM ContactRequest r"
            + "  ORDER BY r.created DESC"),
    @NamedQuery(name = "ContactRequest.findByObjectID",
        query = "SELECT r"
            + "  FROM ContactRequest r"
            + "  WHERE r.objectID = :objectID"
            + "  ORDER BY r.created DESC"),
    @NamedQuery(name = "ContactRequest.findByUUID",
        query = "SELECT r"
            + "  FROM ContactRequest r"
            + "  WHERE r.UUID = :uuid"),
    @NamedQuery(name = "ContactRequest.findByState",
        query = "SELECT r"
            + "  FROM ContactRequest r"
            + "  WHERE r.state = :state"
            + "  ORDER BY r.created DESC"),
})

/**
 * This class defines a model for a request.
 */
@Entity
@Table(name = "contact_request")
public class ContactRequest {

    private static final String PROP_SENDER = "email";

    private static final String PROP_MESSAGE = "message";

    private static final String PROP_NAME = "name";

    private static final String PROP_ORCID = "orcid";

    private static final String PROP_SEND_COPY = "sendCopy";

    /**
     * Internal id.
     */
    private long id;

    /**
     * Mail of requester.
     */
    @Email
    @NotNull
    private String sender;

    /**
     * Name of requester.
     */
    @NotNull
    private String name;

    /**
     * Message of requester.
     */
    @NotNull
    private String message;

    /**
     * Orcid of requester.
     */
    private String orcid; // TODO validation

    /**
     * Linked object of request.
     */
    @NotNull
    private MCRObjectID objectID;

    /**
     * If requester wished copy.
     */
    private boolean sendCopy = false;

    /**
     * Date of creation.
     */
    private Date created;

    /**
     * Name of creator.
     */
    private String createdBy;

    /**
     * Date of last modification.
     */
    private Date lastModified;

    /**
     * Name of the last modifier.
     */
    private String lastModifiedBy;

    /**
     * State of request.
     */
    private ContactRequestState state;

    /**
     * List of recipients.
     */
    private List<ContactRecipient> recipients = new ArrayList();

    /**
     * Generic comment field for request.
     */
    private String comment;

    /**
     * Uuid of request.
     */
    @Column(name = "uuid", unique = true, updatable = false, nullable = false, columnDefinition = "binary(16)")
    private UUID uuid;

    public ContactRequest() { }

    public ContactRequest(MCRObjectID objectID, String sender, String name, String message) {
        this.objectID = objectID;
        this.sender = sender;
        this.name = name;
        this.message = message;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_request_id",
        nullable = false)
    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonProperty
    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "object_id",
        length = MCRObjectID.MAX_LENGTH,
        nullable = false)
    @Convert(converter = MCRObjectIDConverter.class)
    public MCRObjectID getObjectID() {
        return this.objectID;
    }

    public void setObjectID(MCRObjectID objectID) {
        this.objectID = objectID;
    }

    @JsonProperty(value = PROP_SENDER, required = true)
    @Column(name = "sender",
        nullable = false)
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty(value = PROP_MESSAGE, required = true)
    @Column(name = "message",
        nullable = false)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty(value = PROP_NAME, required = true)
    @Column(name = "name",
        nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(value = PROP_ORCID)
    @Column(name = "orcid")
    public String getORCID() {
        return orcid;
    }

    public void setORCID(String orcid) {
        this.orcid = orcid;
    }

    @JsonProperty(value = PROP_SEND_COPY)
    @Column(name = "send_copy",
        nullable = false)
    public boolean isSendCopy() {
        return this.sendCopy;
    }

    public void setSendCopy(boolean sendCopy) {
        this.sendCopy = sendCopy;
    }

    @Column(name = "created")
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Column(name = "createdBy")
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "lastModified")
    public Date getLastModified() {
        return lastModified;
    }

    @Column(name = "lastModified")
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Column(name = "lastModifiedBy")
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Enumerated(EnumType.STRING)
    public ContactRequestState getState() {
        return state;
    }

    @PrePersist
    protected void prepersistUUIDModel() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public void setState(ContactRequestState state) {
        this.state = state;
    }

    @OneToMany(fetch = FetchType.EAGER,
              mappedBy = "request",
              cascade = CascadeType.ALL,
              orphanRemoval = true)
    public List<ContactRecipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<ContactRecipient> recipients) {
        this.recipients = recipients;
    }

    public void addRecipient(ContactRecipient recipient) {
        recipients.add(recipient);
        recipient.setRequest(this);
    }

    public void removeRecipient(ContactRecipient recipient) {
        recipients.remove(recipient);
        recipient.setRequest(null);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
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
        ContactRequest other = (ContactRequest) obj;
        return Objects.equals(uuid, other.getUUID());
    }

    @Override
    public String toString() {
        String result = "";
        if (sender != null) {
            result += "sender: " + sender + "\n";
        }
        if (message != null) {
            result += "message: " + message + "\n";
        }
        if (name != null) {
            result += "name: " + name + "\n";
        }
        result += "state: " + state.toString() + "\n";
        if (orcid != null) {
            result += "orcid: " + orcid + "\n";
        }
        result += "send copy: " + String.valueOf(sendCopy);
        return result;
    }
}
