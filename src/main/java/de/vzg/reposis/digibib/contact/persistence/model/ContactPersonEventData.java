package de.vzg.reposis.digibib.contact.persistence.model;

import java.util.Date;
import java.util.Objects;

import de.vzg.reposis.digibib.contact.model.ContactPersonEvent;
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
 * This class defines a model for a request event.
 */
@Entity
@Table(name = "contactPersonEvent")
public class ContactPersonEventData {

    /**
     * Internal id.
     */
    private long id;

    private Date date;

    private ContactPersonEvent.EventType type;

    private String comment;

    private ContactPersonData person;

    public ContactPersonEventData() {

    }

    public ContactPersonEventData(Date date, ContactPersonEvent.EventType type) {
        setDate(date);
        setType(type);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "date", nullable = false)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "personId")
    public ContactPersonData getPerson() {
        return person;
    }

    public void setPerson(ContactPersonData recipient) {
        this.person = recipient;
    }

    @Column(name = "type", nullable = false)
    public ContactPersonEvent.EventType getType() {
        return type;
    }

    public void setType(ContactPersonEvent.EventType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, date, id, person, type);
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
        ContactPersonEventData other = (ContactPersonEventData) obj;
        return Objects.equals(comment, other.comment) && Objects.equals(date, other.date) && id == other.id
            && Objects.equals(person, other.person) && type == other.type;
    }

}
