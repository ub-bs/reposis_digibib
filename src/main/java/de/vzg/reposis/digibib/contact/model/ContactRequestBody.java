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

import de.vzg.reposis.digibib.contact.validation.ValidOrcid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Defines contact request body model.
 */
public record ContactRequestBody(@NotNull String fromName, @NotNull @Email String fromMail,
    @ValidOrcid String fromOrcid, String message) {

    @Override
    public String toString() {
        String result = "";
        result += "from: " + fromMail + "\n";
        result += "name: " + fromName + "\n";
        if (message != null) {
            result += "message: " + message + "\n";
        }
        if (fromOrcid != null) {
            result += "orcid: " + fromOrcid + "\n";
        }
        return result;
    }
}
