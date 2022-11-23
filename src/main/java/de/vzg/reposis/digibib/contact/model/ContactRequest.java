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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.validation.ValidORCID;

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
@Table(name = "contactRequest")
public class ContactRequest {

    private static final String PROP_SENDER = "email";

    private static final String PROP_MESSAGE = "message";

    private static final String PROP_NAME = "name";

    private static final String PROP_ORCID = "orcid";

    /**
     * Internal id.
     */
    private long id;

    /**
     * Mail of requester.
     */
    @Email
    @NotNull
    private String from;

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
    @ValidORCID
    private String orcid;

    /**
     * Linked object of request.
     */
    @NotNull
    private MCRObjectID objectID;

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
     * Date of forwarding.
     */
    private Date forwarded;

    /**
     * State of request.
     */
    private int state;

    /**
     * List of recipients.
     */
    private List<ContactRecipient> recipients = new ArrayList();

    /**
     * Debug field for internal purposes.
     */
    private String debug;

    /**
     * Comment field for editors
     */
    private String comment;

    /**
     * Uuid of request.
     */
    @Column(name = "uuid", unique = true, updatable = false, nullable = false, columnDefinition = "binary(16)")
    private UUID uuid;

    public ContactRequest() {
    }

    public ContactRequest(MCRObjectID objectID, String from, String name, String message) {
        this.objectID = objectID;
        this.from = from;
        this.name = name;
        this.message = message;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contactId", nullable = false)
    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonProperty
    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "objectId", length = MCRObjectID.MAX_LENGTH, nullable = false)
    @Convert(converter = MCRObjectIDConverter.class)
    public MCRObjectID getObjectID() {
        return this.objectID;
    }

    public void setObjectID(MCRObjectID objectID) {
        this.objectID = objectID;
    }

    @JsonProperty(value = PROP_SENDER, required = true)
    @Column(name = "sender", nullable = false)
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @JsonProperty(value = PROP_MESSAGE, required = true)
    @Column(name = "message", nullable = false)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty(value = PROP_NAME, required = true)
    @Column(name = "name", nullable = false)
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

    public Date getForwarded() {
        return forwarded;
    }

    public void setForwarded(Date forwarded) {
        this.forwarded = forwarded;
    }

    public ContactRequestState getState() {
        return ContactRequestState.resolve(state);
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
        this.state = state.getValue();
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
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
        if (from != null) {
            result += "from: " + from + "\n";
        }
        if (message != null) {
            result += "message: " + message + "\n";
        }
        if (name != null) {
            result += "name: " + name + "\n";
        }
        result += "state: " + state + "\n";
        if (orcid != null) {
            result += "orcid: " + orcid + "\n";
        }
        return result;
    }
}
