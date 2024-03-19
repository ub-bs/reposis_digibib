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

/**
 * Generic repository interface.
 *
 * @param <T> repository entity
 */
public interface ContactBaseRepository<T> {

    /**
     * Returns all entities.
     *
     * @return collection of entity
     */
    Collection<T> findAll();

    /**
     * Returns optional with entity by id.
     *
     * @param id internal id
     * @return optional with entity
     */
    Optional<T> findById(long id);

    /**
     * Inserts entity.
     *
     * @param object entity
     */
    void insert(T object);

    /**
     * Removes entity.
     *
     * @param object entity
     */
    void remove(T object);

    /**
     * Updates entity.
     *
     * @param object entity
     */
    void save(T object);
}
