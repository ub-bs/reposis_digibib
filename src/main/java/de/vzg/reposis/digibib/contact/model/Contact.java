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
import java.util.List;
import java.util.Objects;

import jakarta.validation.constraints.NotNull;

/**
 * Defines contact model.
 */
public class Contact {

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String origin;

    private String reference;

    private List<ContactEvent> events = new ArrayList<>();

    /**
     * Constructs new contact with name, email, origin and reference.
     *
     * @param name name
     * @param email email
     * @param origin orgin
     * @param reference reference
     */
    public Contact(String name, String email, String origin, String reference) {
        setName(name);
        setEmail(email);
        setOrigin(origin);
        setReference(reference);
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
     * Returns mail address.
     *
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email address.
     *
     * @param email email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns origin.
     *
     * @return origin
     */
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
     * Returns list of event elements.
     *
     * @return list of event elements
     */
    public List<ContactEvent> getEvents() {
        return events;
    }

    /**
     * Sets list of event elements.
     *
     * @param events list of event elements
     */
    public void setEvents(List<ContactEvent> events) {
        this.events = events;
    }

    /**
     * Adds event to events.
     *
     * @param event event
     */
    public void addEvent(ContactEvent event) {
        events.add(event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events, email, name, origin, reference);
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
        Contact other = (Contact) obj;
        return Objects.equals(events, other.events) && Objects.equals(email, other.email)
            && Objects.equals(reference, other.reference) && Objects.equals(name, other.name)
            && Objects.equals(origin, other.origin);
    }
}
