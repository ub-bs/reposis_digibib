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

import org.mycore.datamodel.metadata.MCRObjectID;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;

/**
 * Defines {@link ContactBaseRepository} interface for {@link ContactRequestData}.
 */
public interface ContactRequestRepository extends ContactBaseRepository<ContactRequestData> {

    /**
     * Returns a collection over {@link ContactRequestData} elements by object id.
     *
     * @param objectId object id
     * @return collection over contact request data elements
     */
    Collection<ContactRequestData> findByObjectId(MCRObjectID objectId);

    /**
     * Returns a request collection that are in given state.
     *
     * @param state state
     * @return collection over contact request data elements
     */
    Collection<ContactRequestData> findByState(ContactRequest.State state);

    /**
     * Returns optional with request by given uuid.
     *
     * @param uuid uuid
     * @return optional with request
     */
    Optional<ContactRequestData> findByUuid(UUID uuid);
}
