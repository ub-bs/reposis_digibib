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

/**
 * Generic intefaces which defines dao methods for an entity.
 */
public interface ContactBaseDAO<T> {

    /**
     * Returns all entities.
     * @return collection of entity
     */
    Collection<T> findAll();

    /**
     * Returns entity by id.
     * @param id internal id
     * @return the entity or null
     */
    T findByID(long id);

    /**
     * Inserts entity.
     * @param object the entity
     */ 
    void insert(T object);

    /**
     * Updates an entity.
     * @param object the entity
     */ 
    void update(T object);

    /**
     * Removes an entity.
     * @param object the entity
     */ 
    void remove(T object);
}
