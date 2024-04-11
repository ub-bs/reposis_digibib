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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.mycore.backend.jpa.MCRObjectIDConverter;
import org.mycore.datamodel.metadata.MCRObjectID;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@NamedQueries({
    @NamedQuery(name = "ContactRequest.findAll", query = "SELECT r"
        + "  FROM ContactRequestData r"
        + "  ORDER BY r.created DESC"),
    @NamedQuery(name = "ContactRequest.findByObjectId", query = "SELECT r"
        + "  FROM ContactRequestData r"
        + "  WHERE r.objectId = :objectId"
        + "  ORDER BY r.created DESC"),
    @NamedQuery(name = "ContactRequest.findByUuid", query = "SELECT r"
        + "  FROM ContactRequestData r"
        + "  WHERE r.uuid = :uuid"),
    @NamedQuery(name = "ContactRequest.findByState", query = "SELECT r"
        + "  FROM ContactRequestData r"
        + "  WHERE r.status = :status"
        + "  ORDER BY r.created DESC"),
})

/**
 * This class defines a model for a request.
 */
@Entity
@Table(name = "contactRequest")
public class ContactRequestData {

    private long id;

    private String email;

    private String name;

    private String message;

    private String orcid;

    private MCRObjectID objectId;

    private Date created;

    private String createdBy;

    @Enumerated(EnumType.STRING)
    private ContactRequest.RequestStatus status;

    private List<ContactData> contacts = new ArrayList<>();

    private String comment;

    private UUID uuid;

    /**
     * Returns internal id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contactId", nullable = false)
    public long getId() {
        return id;
    }

    /**
     * Sets internal id.
     *
     * @param id id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns object id.
     *
     * @return object id
     */
    @Column(name = "objectId", length = MCRObjectID.MAX_LENGTH, nullable = false)
    @Convert(converter = MCRObjectIDConverter.class)
    public MCRObjectID getObjectId() {
        return objectId;
    }

    /**
     * Sets object id.
     *
     * @param objectId object id
     */
    public void setObjectId(MCRObjectID objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns from email.
     *
     * @return email
     */
    @Column(name = "sender", nullable = false)
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
    @Column(name = "message", nullable = false)
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
    @Column(name = "name", nullable = false)
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
    @Column(name = "orcid")
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

    /**
     * Returns date of creation.
     *
     * @return date of creation
     */
    @Column(name = "created")
    public Date getCreated() {
        return created;
    }

    /**
     * Sets date of creation.
     *
     * @param created date of creation
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Returns name of created by.
     *
     * @return name of created by
     */
    @Column(name = "createdBy")
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets name of created by.
     *
     * @param createdBy name of created by
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns status.
     *
     * @return status
     */
    public ContactRequest.RequestStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status status
     */
    public void setStatus(ContactRequest.RequestStatus status) {
        this.status = status;
    }

    @PrePersist
    protected void prepersistUUIDModel() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    /**
     * Returns uuid.
     *
     * @return uuid
     */
    @Column(name = "uuid", unique = true, updatable = false, nullable = false)
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Sets uuid.
     *
     * @param uuid uuid
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns contacts.
     *
     * @return list of contact elements.
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ContactData> getContacts() {
        return contacts;
    }

    /**
     * Sets contacts.
     *
     * @param contacts list of contact elements.
     */
    public void setContacts(List<ContactData> contacts) {
        this.contacts = contacts;
    }

    /**
     * Adds contact.
     *
     * @param contact contact
     */
    public void addContact(ContactData contact) {
        contacts.add(contact);
        contact.setRequest(this);
    }

    /**
     * Removes contact.
     *
     * @param contact contact
     */
    public void removeContact(ContactData contact) {
        contacts.remove(contact);
        contact.setRequest(null);
    }

    /**
     * Returns comment.
     *
     * @return comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets comment.
     *
     * @param comment comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, created, createdBy, email, id, message, name, objectId, orcid, contacts, status,
            uuid);
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
        ContactRequestData other = (ContactRequestData) obj;
        return Objects.equals(comment, other.comment) && Objects.equals(created, other.created)
            && Objects.equals(createdBy, other.createdBy) && Objects.equals(email, other.email) && id == other.id
            && Objects.equals(message, other.message) && Objects.equals(name, other.name)
            && Objects.equals(objectId, other.objectId) && Objects.equals(orcid, other.orcid)
            && Objects.equals(contacts, other.contacts) && status == other.status
            && Objects.equals(uuid, other.uuid);
    }
}
