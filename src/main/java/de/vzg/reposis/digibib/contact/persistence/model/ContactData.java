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
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Defines contact data model.
 */
@Entity
@Table(name = "contact")
public class ContactData {

    private long id;

    private String name;

    private String email;

    private String origin;

    private String reference;

    private ContactRequestData request;

    private List<ContactEventData> events = new ArrayList<>();

    /**
     * Returns internal id.
     *
     * @return internal id
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
     * @param id internal id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns email.
     *
     * @return email
     */
    @Column(name = "mail", nullable = false)
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns origin.
     *
     * @return origin.
     */
    @Column(name = "type", nullable = false)
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets origin.
     *
     * @param origin origin
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * Returns origin reference.
     *
     * @return reference
     */
    @Column(name = "reference", nullable = true)
    public String getReference() {
        return reference;
    }

    /**
     * Sets origin reference.
     *
     * @param reference reference
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Returns request.
     *
     * @return request
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requestId")
    public ContactRequestData getRequest() {
        return request;
    }

    /**
     * Sets request.
     *
     * @param request request
     */
    public void setRequest(ContactRequestData request) {
        this.request = request;
    }

    /**
     * Returns list of event elements.
     *
     * @return list of event elements
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ContactEventData> getEvents() {
        return events;
    }

    /**
     * Sets list of event elements.
     *
     * @param events list of event elements
     */
    public void setEvents(List<ContactEventData> events) {
        this.events = events;
    }

    /**
     * Adds event.
     *
     * @param event event
     */
    public void addEvent(ContactEventData event) {
        events.add(event);
        event.setContact(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, origin, request, reference);
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
        ContactData other = (ContactData) obj;
        return id == other.id && Objects.equals(email, other.email) && Objects.equals(name, other.name)
            && Objects.equals(reference, other.reference) && origin == other.origin
            && Objects.equals(request, other.request);
    }
}
