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

import com.fasterxml.jackson.annotation.JsonIgnore;

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
 * This class defines a model for a recipient.
 */
@Entity
@Table(name = "contactPerson")
public class ContactPersonData {

    /**
     * Internal id.
     */
    private long id;

    /**
     * Name of the person.
     */
    private String name;

    /**
     * Origin of person date.
     */
    private String origin;

    /**
     * Person mail.
     */
    private String mail;

    /**
     * Parent request of person.
     */
    private ContactRequestData request;

    private List<ContactPersonEventData> events = new ArrayList<>();

    public ContactPersonData() {
    }

    /**
     * @return internal id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personId", nullable = false)
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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ContactPersonEventData> getEvents() {
        return events;
    }

    public void setEvents(List<ContactPersonEventData> events) {
        this.events = events;
    }

    public void addEvent(ContactPersonEventData event) {
        events.add(event);
        event.setPerson(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mail, name, origin, request);
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
        ContactPersonData other = (ContactPersonData) obj;
        return id == other.id && Objects.equals(mail, other.mail) && Objects.equals(name, other.name)
            && origin == other.origin && Objects.equals(request, other.request);
    }
}
