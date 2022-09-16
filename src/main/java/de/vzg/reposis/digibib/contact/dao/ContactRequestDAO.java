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

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;

import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * This interfaces defindes methods for a request dao.
 */
public interface ContactRequestDAO extends ContactBaseDAO<ContactRequest> {

    /**
     * Returns a request collection by object id.
     * @param objectID the object id
     * @return the request collection
     */
    Collection<ContactRequest> findByObjectID(MCRObjectID objectID);

    /**
     * Returns a request collection that are in given state.
     * @param state the state
     * @return the request collection
     */
    Collection<ContactRequest> findByState(ContactRequestState state);

    /**
     * Returns a request by given uuid.
     * @param uuid the uuid
     * @return the request or null
     */
    ContactRequest findByUUID(UUID uuid);
}
