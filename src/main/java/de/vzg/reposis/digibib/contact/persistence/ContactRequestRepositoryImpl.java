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

package de.vzg.reposis.digibib.contact.persistence;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.datamodel.metadata.MCRObjectID;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * Implements {@link ContactRequestRepository}.
 */
public class ContactRequestRepositoryImpl implements ContactRequestRepository {

    @Override
    public Collection<ContactRequestData> findAll() {
        return getEntityManager().createNamedQuery("ContactRequest.findAll", ContactRequestData.class).getResultList();
    }

    @Override
    public Optional<ContactRequestData> findById(long id) {
        return Optional.ofNullable(getEntityManager().find(ContactRequestData.class, id));
    }

    @Override
    public Collection<ContactRequestData> findByObjectId(MCRObjectID objectID) {
        return getEntityManager().createNamedQuery("ContactRequest.findByObjectId", ContactRequestData.class)
            .setParameter("objectID", objectID).getResultList();
    }

    @Override
    public Collection<ContactRequestData> findByState(ContactRequest.State state) {
        return getEntityManager().createNamedQuery("ContactRequest.findByState", ContactRequestData.class)
            .setParameter("state", state).getResultList();
    }

    @Override
    public Optional<ContactRequestData> findByUuid(UUID uuid) {
        final TypedQuery<ContactRequestData> query = getEntityManager()
            .createNamedQuery("ContactRequest.findByUuid", ContactRequestData.class).setParameter("uuid", uuid);
        try {
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void insert(ContactRequestData request) {
        final EntityManager entityManager = getEntityManager();
        entityManager.persist(request);
    }

    @Override
    public void remove(ContactRequestData request) {
        final EntityManager entityManager = getEntityManager();
        entityManager.remove(entityManager.merge(request));
    }

    @Override
    public void save(ContactRequestData request) {
        final EntityManager entityManager = getEntityManager();
        entityManager.merge(request);
    }

    private EntityManager getEntityManager() {
        return MCREntityManagerProvider.getCurrentEntityManager();
    }
}
