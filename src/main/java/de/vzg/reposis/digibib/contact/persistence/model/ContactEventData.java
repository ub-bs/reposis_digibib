package de.vzg.reposis.digibib.contact.persistence.model;

import java.util.Date;
import java.util.Objects;

import de.vzg.reposis.digibib.contact.model.ContactEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Defines contact event data model.
 */
@Entity
@Table(name = "contactEvent")
public class ContactEventData {

    private long id;

    private ContactEvent.EventType type;

    private ContactData contact;

    private Date date;

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
     * Returns internal id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId", nullable = false)
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
     * Returns event type.
     *
     * @return type
     */
    @Column(name = "type", nullable = false)
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
     * Returns contact.
     *
     * @return contact
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contactId")
    public ContactData getContact() {
        return contact;
    }

    /**
     * Sets contact.
     *
     * @param contact contact
     */
    public void setContact(ContactData contact) {
        this.contact = contact;
    }

    /**
     * Returns date.
     *
     * @return date
     */
    @Column(name = "date", nullable = false)
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
        return Objects.hash(id, type, contact, date, comment);
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
        return Objects.equals(comment, other.comment) && Objects.equals(date, other.date) && id == other.id
            && Objects.equals(contact, other.contact) && type == other.type;
    }

}
