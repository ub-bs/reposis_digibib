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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.mycore.datamodel.metadata.MCRObjectID;

import jakarta.validation.constraints.NotNull;

/**
 * Defines request model.
 */
public class ContactRequest {

    private UUID id;

    @NotNull
    private MCRObjectID objectId;

    @NotNull
    private ContactRequestBody body;

    private Date created;

    private String createdBy;

    @NotNull
    private RequestStatus status;

    private List<Contact> contacts = new ArrayList<Contact>();

    private String comment;

    /**
     * Constructs contact request with body.
     *
     * @param body body
     */
    public ContactRequest(ContactRequestBody body) {
        setBody(body);
    }

    /**
     * Returns request id.
     *
     * @return id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets request id.
     *
     * @param id id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns linked object id.
     *
     * @return object id
     */
    public MCRObjectID getObjectId() {
        return objectId;
    }

    /**
     * Sets linked object id.
     *
     * @param objectId object id
     */
    public void setObjectId(MCRObjectID objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns request body.
     *
     * @return request body
     */
    public ContactRequestBody getBody() {
        return body;
    }

    /**
     * Sets request body.
     *
     * @param body request body
     */
    public void setBody(ContactRequestBody body) {
        this.body = body;
    }

    /**
     * Return date of creation.
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
     * Returns name of creator.
     *
     * @return name of creator
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets name of creator.
     *
     * @param createdBy name of creator
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns request state.
     *
     * @return state
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Sets request state.
     *
     * @param status state
     */
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    /**
     * Returns list of contact elements.
     *
     * @return list of contact elements
     */
    public List<Contact> getContacts() {
        return contacts;
    }

    /**
     * Sets list of contact elements.
     *
     * @param contacts list of contact elements
     */
    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Adds contact.
     *
     * @param contact contact
     */
    public void addContact(Contact contact) {
        contacts.add(contact);
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
        return Objects.hash(comment, created, createdBy, contacts, status, id);
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
        return Objects.equals(comment, other.comment) && Objects.equals(created, other.created)
            && Objects.equals(createdBy, other.createdBy) && Objects.equals(id, other.id)
            && Objects.equals(contacts, other.contacts) && status == other.status;
    }

    /**
     * Describes possible request states.
     */
    public enum RequestStatus {

        /**
         * Received.
         */
        RECEIVED(0),

        /**
         * Processed.
         */
        PROCESSED(10),

        /**
         * Completed.
         */
        COMPLETED(20);

        private final int value;

        RequestStatus(int value) {
            this.value = value;
        }

        /**
         * Resolves and returns status by value.
         *
         * @param value value
         * @return status
         */
        public static RequestStatus resolve(int value) {
            return Arrays.stream(values()).filter(o -> o.value == value).findFirst().orElse(null);
        }

        /**
         * Returns value.
         *
         * @return value
         */
        public int getValue() {
            return value;
        }
    }

}
