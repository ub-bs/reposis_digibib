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

import java.util.Date;
import java.util.Objects;

import de.vzg.reposis.digibib.contact.model.ContactEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Defines contact event data model.
 */
@Embeddable
public class ContactEventData {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ContactEvent.EventType type;

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "comment", nullable = true)
    private String comment;

    /**
     * Constructs new contact event data.
     */
    public ContactEventData() {

    }

    /**
     * Constructs new contact event data with date and type.
     *
     * @param date date
     * @param type type
     */
    public ContactEventData(ContactEvent.EventType type, Date date) {
        setType(type);
        setDate(date);
    }

    /**
     * Returns event type.
     *
     * @return type
     */
    public ContactEvent.EventType getType() {
        return type;
    }

    /**
     * Sets event type.
     *
     * @param type type
     */
    public void setType(ContactEvent.EventType type) {
        this.type = type;
    }

    /**
     * Returns date.
     *
     * @return date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets date.
     *
     * @param date date
     */
    public void setDate(Date date) {
        this.date = date;
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
        return Objects.hash(type, date, comment);
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
        ContactEventData other = (ContactEventData) obj;
        return Objects.equals(comment, other.comment) && Objects.equals(date, other.date) && type == other.type;
    }

}
