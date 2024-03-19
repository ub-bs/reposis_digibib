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

import java.util.Date;

/**
 * Defines forwarding model.
 */
public class ContactForwarding {

    private Date date;

    private Date failed;

    private Date success;

    /**
     * Returns date of fail.
     *
     * @return date of fail
     */
    public Date getFailed() {
        return failed;
    }

    /**
     * Sets date of fail
     *
     * @param failed date of fail
     */
    public void setFailed(Date failed) {
        this.failed = failed;
    }

    /**
     * Returns date of sent.
     *
     * @return date of sent
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets date of sent.
     *
     * @param sent date of sent
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns date of confirm.
     *
     * @return date of confirm
     */
    public Date getConfirmed() {
        return success;
    }

    /**
     * Sets date of confirm.
     *
     * @param confirmed date of confirm
     */
    public void setConfirmed(Date confirmed) {
        this.success = confirmed;
    }
}
