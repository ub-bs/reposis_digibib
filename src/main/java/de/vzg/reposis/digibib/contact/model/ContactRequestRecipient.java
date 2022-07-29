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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "contact_request_recipient")
public class ContactRequestRecipient {

    private long id;

    private String name;

    private RecipientSource recipientSource;

    private String email;

    private ContactRequest contactRequest;

    public ContactRequestRecipient() { }

    public ContactRequestRecipient(String name, RecipientSource source, String email) {
        this.name = name;
        this.recipientSource = source;
        this.email = email;
    }

    public ContactRequestRecipient(RecipientSource source, String email) {
        this.recipientSource = source;
        this.email = email;
    }

    /**
     * @return internal id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id",
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type",
        nullable = false)
    public RecipientSource getRecipientSource() {
        return recipientSource;
    }

    public void setRecipientSource(RecipientSource source) {
        this.recipientSource = source;
    }

    @Column(name = "email",
        nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="contact_request_id")
    public ContactRequest getContactRequest() {
        return contactRequest;
    }

    public void setContactRequest(ContactRequest contactRequest) {
        this.contactRequest = contactRequest;
    }
}