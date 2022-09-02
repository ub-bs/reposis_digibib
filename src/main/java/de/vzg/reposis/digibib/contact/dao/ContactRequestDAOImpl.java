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

package de.vzg.reposis.digibib.contact.dao;

import java.util.Collection;
import java.util.UUID;

import javax.persistence.EntityManager;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;

import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ContactRequestDAOImpl implements ContactRequestDAO {

    @Override
    public Collection<ContactRequest> findAll() {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        return MCREntityManagerProvider.getCurrentEntityManager()
                .createNamedQuery("ContactRequest.findAll", ContactRequest.class).getResultList();
    }

    @Override
    public Collection<ContactRequest> findByObjectID(MCRObjectID objectID) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        return entityManager.createNamedQuery("ContactRequest.findByObjectID", ContactRequest.class)
                .setParameter("objectID", objectID).getResultList();
    }

    @Override
    public Collection<ContactRequest> findByState(ContactRequestState state) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        return MCREntityManagerProvider.getCurrentEntityManager()
                .createNamedQuery("ContactRequest.findByState", ContactRequest.class)
                .setParameter("state", state).getResultList();
    }

    @Override
    public ContactRequest findByID(long id) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        return entityManager.find(ContactRequest.class, id);
    }

    @Override
    public ContactRequest findByUUID(UUID uuid) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        final Collection<ContactRequest> requests = entityManager
                .createNamedQuery("ContactRequest.findByUUID", ContactRequest.class)
                .setParameter("uuid", uuid).getResultList(); // should contain at most one element
        return requests.stream().findFirst().orElse(null);
    }

    @Override
    public void insert(ContactRequest request) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        entityManager.persist(request);
        entityManager.flush();
    }

    @Override
    public void update(ContactRequest request) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        entityManager.merge(request);
        entityManager.flush();
    }

    @Override
    public void remove(ContactRequest request) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        entityManager.remove(entityManager.merge(request));
        entityManager.flush();
    }
}
