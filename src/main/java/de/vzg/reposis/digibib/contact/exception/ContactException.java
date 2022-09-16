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

package de.vzg.reposis.digibib.contact.exception;

import org.mycore.common.MCRException;

/**
 * Parent class for contact exception.
 */
public class ContactException extends MCRException {

    /**
     * The internal errorCode which is also important for frontend.
     */
    private String errorCode;

    public ContactException(String message) {
        super(message);
        errorCode = "contactError";
    }

    public ContactException(Throwable cause) {
        super(cause);
        errorCode = "contactError";
    }

    public ContactException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
