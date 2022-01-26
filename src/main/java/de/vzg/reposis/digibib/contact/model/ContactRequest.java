package de.vzg.reposis.digibib.contact.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.mycore.backend.jpa.MCRObjectIDConverter;
import org.mycore.datamodel.metadata.MCRObjectID;

@NamedQueries({
    @NamedQuery(name = "ContactRequest.findAll",
        query = "SELECT r"
            + "  FROM ContactRequest r"),
    @NamedQuery(name = "ContactRequest.findByObjectID",
        query = "SELECT r"
            + "  FROM ContactRequest r"
            + "  WHERE r.objectID = :objectID"
            + "  ORDER BY r.lastModified ASC"),
    @NamedQuery(name = "ContactRequest.findByState",
        query = "SELECT r"
            + "  FROM ContactRequest r"
            + "  WHERE r.state = :state"
            + "  ORDER BY r.lastModified ASC"),
})

@Entity
@Table(name = "ContactRequest")
public class ContactRequest {

    private static final String PROP_SENDER = "email";

    private static final String PROP_MESSAGE = "message";

    private static final String PROP_NAME = "name";

    private static final String PROP_ORCID = "orcid";

    private static final String PROP_SEND_COPY = "sendCopy";

    private long id;

    private String sender;

    private String name;

    private String message;

    private String orcid;

    private MCRObjectID objectID;

    private boolean sendCopy = false;

    /** The date of creation */
    private Date created;

    /** The name of creator */
    private String createdBy;

    /** The date of last modification */
    private Date lastModified;

    /** The name of the last modifier */
    private String lastModifiedBy;

    private ContactRequestState state;

    public ContactRequest() { }

    public ContactRequest(String sender, String name, String message, String orcid) {
        this.sender = sender;
        this.name = name;
        this.message = message;
        this.orcid = orcid;
    }

    /**
     * @return internal id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_request_id",
        nullable = false)
    public long getId() {
        return id;
    }

    /**
     * @param id internal id
     */
    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "object_id",
        length = MCRObjectID.MAX_LENGTH,
        nullable = false)
    @Convert(converter = MCRObjectIDConverter.class)
    public MCRObjectID getObjectID() {
        return this.objectID;
    }

    public void setObjectID(MCRObjectID objectID) {
        this.objectID = objectID;
    }

    @JsonProperty(value = PROP_SENDER, required = true)
    @Column(name = "sender",
        nullable = false)
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty(value = PROP_MESSAGE, required = true)
    @Column(name = "message",
        nullable = false)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty(value = PROP_NAME, required = true)
    @Column(name = "name",
        nullable = false)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @JsonProperty(value = PROP_ORCID)
    @Column(name = "orcid")
    public String getORCID() {
        return orcid;
    }

    public void setORCID(final String orcid) {
        this.orcid = orcid;
    }

    @JsonProperty(value = PROP_SEND_COPY)
    @Column(name = "send_copy",
        nullable = false)
    public boolean getSendCopy() {
        return this.sendCopy;
    }

    public void setSendCopy(boolean sendCopy) {
        this.sendCopy = sendCopy;
    }

    /**
     * @return date of creation
     */
    @Column(name = "created")
    public Date getCreated() {
        return created;
    }

    @PrePersist
    protected void onCreate() {
        created = new Date();
        lastModified = new Date();
    }

    /**
     * @param created date of creation
     */
    public void setCreated(final Date created) {
        this.created = created;
    }

    /**
     * @return name of creator
     */
    @Column(name = "createdBy")
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy name of creator
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return date of last modification
     */
    @Column(name = "lastModified")
    public Date getLastModified() {
        return lastModified;
    }

    @PreUpdate
    protected void onUpdate() {
        lastModified = new Date();
    }

    /**
     * @param lastModified date of last modification
     */
    @Column(name = "lastModified")
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return name of last modifier
     */
    @Column(name = "lastModifiedBy")
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * @param lastModifiedBy name of last modifier
     */
    public void setLastModifiedBy(final String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Enumerated(EnumType.STRING)
    public ContactRequestState getState() {
        return state;
    }

    public void setState(ContactRequestState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        String result = "";
        if (sender != null) {
            result += "sender: " + sender + "\n";
        }
        if (message != null) {
            result += "message: " + message + "\n";
        }
        if (name != null) {
            result += "name: " + name + "\n";
        }
        if (orcid != null) {
            result += "orcid: " + orcid + "\n";
        }
        result += "send copy: " + String.valueOf(sendCopy);
        return result;
    }
}
