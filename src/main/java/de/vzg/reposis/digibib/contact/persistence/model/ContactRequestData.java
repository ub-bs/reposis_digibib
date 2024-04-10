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

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.validation.ValidOrcid;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

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
        + "  WHERE r.state = :state"
        + "  ORDER BY r.created DESC"),
})

/**
 * This class defines a model for a request.
 */
@Entity
@Table(name = "contactRequest")
public class ContactRequestData {

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
     * State of request.
     */
    @Enumerated(EnumType.STRING)
    private ContactRequest.RequestStatus state;

    /**
     * List of recipients.
     */
    private List<ContactPersonData> persons = new ArrayList<ContactPersonData>();

    /**
     * Comment field for editors.
     */
    private String comment;

    /**
     * Uuid of request.
     */
    @Column(name = "uuid", unique = true, updatable = false, nullable = false, columnDefinition = "binary(16)")
    private UUID uuid;

    /**
     * Constructs new {@link ContactRequestData}.
     */
    public ContactRequestData() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contactId", nullable = false)
    public long getId() {
        return id;
    }

    @JsonProperty
    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "objectId", length = MCRObjectID.MAX_LENGTH, nullable = false)
    @Convert(converter = MCRObjectIDConverter.class)
    public MCRObjectID getObjectId() {
        return objectId;
    }

    public void setObjectId(MCRObjectID objectID) {
        this.objectId = objectID;
    }

    @Column(name = "sender", nullable = false)
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Column(name = "message", nullable = false)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "orcid")
    public String getOrcid() {
        return orcid;
    }

    public void setOrcid(String orcid) {
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

    public ContactRequest.RequestStatus getState() {
        return state;
    }

    public void setState(ContactRequest.RequestStatus state) {
        this.state = state;
    }

    @PrePersist
    protected void prepersistUUIDModel() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ContactPersonData> getPersons() {
        return persons;
    }

    public void setPersons(List<ContactPersonData> recipients) {
        this.persons = recipients;
    }

    public void addPerson(ContactPersonData recipient) {
        persons.add(recipient);
        recipient.setRequest(this);
    }

    public void removePerson(ContactPersonData recipient) {
        persons.remove(recipient);
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
        return Objects.hash(comment, created, createdBy, from, id, message, name, objectId, orcid, persons, state,
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
            && Objects.equals(createdBy, other.createdBy) && Objects.equals(from, other.from) && id == other.id
            && Objects.equals(message, other.message) && Objects.equals(name, other.name)
            && Objects.equals(objectId, other.objectId) && Objects.equals(orcid, other.orcid)
            && Objects.equals(persons, other.persons) && state == other.state
            && Objects.equals(uuid, other.uuid);
    }
}
