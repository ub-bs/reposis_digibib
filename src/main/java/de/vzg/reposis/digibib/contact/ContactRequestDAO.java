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

package de.vzg.reposis.digibib.contact;

import java.util.Collection;

import javax.persistence.EntityManager;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;

import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ContactRequestDAO {

    public Collection<ContactRequest> findAll() {
        final Collection<ContactRequest> contactRequests = MCREntityManagerProvider.getCurrentEntityManager()
                .createNamedQuery("ContactRequest.findAll", ContactRequest.class).getResultList();
        return contactRequests;
    }

    public Collection<ContactRequest> findByObjectID(MCRObjectID objectID) {
        final Collection<ContactRequest> contactRequests = MCREntityManagerProvider.getCurrentEntityManager()
                .createNamedQuery("ContactRequest.findByObjectID", ContactRequest.class)
                .setParameter("objectID", objectID).getResultList();
        return contactRequests;
    }

    public Collection<ContactRequest> findByState(ContactRequestState state) {
        final Collection<ContactRequest> contactRequests = MCREntityManagerProvider.getCurrentEntityManager()
                .createNamedQuery("ContactRequest.findByState", ContactRequest.class)
                .setParameter("state", state).getResultList();
        return contactRequests;
    }

    public ContactRequest findByID(long id) {
        final ContactRequest contactRequest = MCREntityManagerProvider.getCurrentEntityManager()
            .find(ContactRequest.class, id);
        return contactRequest;
    }

    public void save(ContactRequest contactRequest) {
        MCREntityManagerProvider.getCurrentEntityManager().persist(contactRequest);
    }

    public void remove(ContactRequest contactRequest) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        entityManager.remove(entityManager.contains(contactRequest) ? contactRequest
                : entityManager.merge(contactRequest));
    }
}
