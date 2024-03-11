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

import org.mycore.datamodel.metadata.MCRObjectID;

import de.vzg.reposis.digibib.contact.validation.ValidOrcid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * This class defines a model for a request.
 */
public class ContactRequest {

    /**
     * Uuid of request.
     */
    private UUID id;

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
    @ValidOrcid
    private String orcid;

    /**
     * Linked object of request.
     */
    @NotNull
    private MCRObjectID objectId;

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
    private List<ContactRecipient> recipients = new ArrayList<ContactRecipient>();

    /**
     * Debug field for internal purposes.
     */
    private String debug;

    /**
     * Comment field for editors
     */
    private String comment;

    public ContactRequest() {
    }

    public ContactRequest(MCRObjectID objectID, String from, String name, String message) {
        objectId = objectID;
        this.from = from;
        this.name = name;
        this.message = message;
    }

    public MCRObjectID getObjectId() {
        return objectId;
    }

    public void setObjectId(MCRObjectID objectId) {
        this.objectId = objectId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setState(ContactRequestState state) {
        this.state = state.getValue();
    }

    public List<ContactRecipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<ContactRecipient> recipients) {
        this.recipients = recipients;
    }

    public void addRecipient(ContactRecipient recipient) {
        recipients.add(recipient);
    }

    public void removeRecipient(ContactRecipient recipient) {
        recipients.remove(recipient);
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
        return Objects.hash(comment, created, createdBy, debug, forwarded, from, lastModified, lastModifiedBy, message,
            name, objectId, orcid, recipients, state, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContactRequest other = (ContactRequest) obj;
        return Objects.equals(comment, other.comment) && Objects.equals(created, other.created)
            && Objects.equals(createdBy, other.createdBy) && Objects.equals(debug, other.debug)
            && Objects.equals(forwarded, other.forwarded) && Objects.equals(from, other.from)
            && Objects.equals(lastModified, other.lastModified)
            && Objects.equals(lastModifiedBy, other.lastModifiedBy) && Objects.equals(message, other.message)
            && Objects.equals(name, other.name) && Objects.equals(objectId, other.objectId)
            && Objects.equals(orcid, other.orcid) && Objects.equals(recipients, other.recipients)
            && state == other.state && Objects.equals(id, other.id);
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
