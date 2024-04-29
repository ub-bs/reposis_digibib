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
import jakarta.persistence.Embedded;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contactId", nullable = false)
    private Long id;

    @Column(name = "objectId", length = MCRObjectID.MAX_LENGTH, nullable = false)
    @Convert(converter = MCRObjectIDConverter.class)
    private MCRObjectID objectId;

    @Column(name = "created")
    private Date created;

    @Column(name = "createdBy")
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContactRequest.RequestStatus status;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactData> contacts = new ArrayList<>();

    @Column(name = "comment", nullable = true)
    private String comment;

    @Column(name = "uuid", unique = true, updatable = false, nullable = false)
    private UUID uuid;

    @Embedded
    private ContactRequestBodyData body;

    /**
     * Returns internal id.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets internal id.
     *
     * @param id id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns object id.
     *
     * @return object id
     */
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
     * Returns date of creation.
     *
     * @return date of creation
     */
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
     * Returns request body.
     *
     * @return body
     */
    public ContactRequestBodyData getBody() {
        return body;
    }

    /**
     * Sets request body.
     *
     * @param body body
     */
    public void setBody(ContactRequestBodyData body) {
        this.body = body;
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
        return Objects.hash(comment, created, createdBy, body, id, objectId, contacts, status, uuid);
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
            && Objects.equals(createdBy, other.createdBy) && Objects.equals(objectId, other.objectId)
            && Objects.equals(contacts, other.contacts) && status == other.status
            && Objects.equals(uuid, other.uuid);
    }
}
