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

/**
 * Defines contact person model.
 */
public class ContactPerson {

    private String name;

    private String mail;

    private String origin;

    private List<ContactPersonEvent> events = new ArrayList<>();

    /**
     * Constructs new recipient with recipient.
     */
    public ContactPerson(String name, String mail, String origin) {
        setName(name);
        setMail(mail);
        setOrigin(origin);
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
     * @return mail address
     */
    public String getMail() {
        return mail;
    }

    /**
     * Sets mail address.
     *
     * @param mail mail address
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Returns origin.
     *
     * @return
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
     * Returns list of event elements.
     *
     * @return list of event elements
     */
    public List<ContactPersonEvent> getEvents() {
        return events;
    }

    /**
     * Sets list of event elements.
     *
     * @param events list of event elements
     */
    public void setEvents(List<ContactPersonEvent> events) {
        this.events = events;
    }

    /**
     * Adds event to events.
     *
     * @param event event
     */
    public void addEvent(ContactPersonEvent event) {
        events.add(event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events, mail, name, origin);
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
        ContactPerson other = (ContactPerson) obj;
        return Objects.equals(events, other.events) && Objects.equals(mail, other.mail)
            && Objects.equals(name, other.name) && Objects.equals(origin, other.origin);
    }
}
