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

import de.vzg.reposis.digibib.contact.model.ContactRecipient;

import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRTransactionHelper;

public class ContactRecipientDAOImpl implements ContactRecipientDAO {

    @Override
    public Collection<ContactRecipient> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContactRecipient findByID(long id) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        final ContactRecipient recipient = entityManager.find(ContactRecipient.class, id);
        if (recipient != null) {
            entityManager.detach(recipient);
        }
        return recipient;
    }

    @Override
    public ContactRecipient findByUUID(UUID uuid) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        final Collection<ContactRecipient> recipients = entityManager
                .createNamedQuery("ContactRecipient.findByUUID", ContactRecipient.class)
                .setParameter("uuid", uuid).getResultList(); // should contain at most one element
        recipients.forEach(entityManager::detach);
        return recipients.stream().findFirst().orElse(null);
    }

    @Override
    public void insert(ContactRecipient recipient) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(ContactRecipient recipient) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ContactRecipient recipient) {
        throw new UnsupportedOperationException();
    }
}
