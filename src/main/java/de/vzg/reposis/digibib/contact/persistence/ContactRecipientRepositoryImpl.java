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

import de.vzg.reposis.digibib.contact.persistence.model.ContactRecipientData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * This class implements a recipient dao.
 */
public class ContactRecipientRepositoryImpl implements ContactRecipientRepository {

    @Override
    public Collection<ContactRecipientData> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ContactRecipientData> findByID(long id) {
        return Optional.ofNullable(getEntityManager().find(ContactRecipientData.class, id));
    }

    @Override
    public Optional<ContactRecipientData> findByUUID(UUID uuid) {
        final TypedQuery<ContactRecipientData> recipient = getEntityManager()
            .createNamedQuery("ContactRecipient.findByUUID", ContactRecipientData.class)
            .setParameter("uuid", uuid);
        try {
            return Optional.ofNullable(recipient.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private EntityManager getEntityManager() {
        return MCREntityManagerProvider.getCurrentEntityManager();
    }

    @Override
    public void insert(ContactRecipientData recipient) {
        final EntityManager entityManager = getEntityManager();
        entityManager.persist(recipient);
        entityManager.flush();
    }

    @Override
    public void remove(ContactRecipientData recipient) {
        final EntityManager entityManager = getEntityManager();
        entityManager.remove(entityManager.merge(recipient));
        entityManager.flush();
    }

    @Override
    public void save(ContactRecipientData recipient) {
        final EntityManager entityManager = getEntityManager();
        entityManager.merge(recipient);
        entityManager.flush();
    }
}
