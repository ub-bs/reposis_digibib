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

package de.vzg.reposis.digibib.contact.validation;

import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * This class provides a validation service.
 */
public class ContactValidator {

    public static ContactValidator getInstance() {
        return Holder.INSTANCE;
    }

    private final Validator validator;

    private ContactValidator() {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Validates a recipient against model.
     *
     * @param recipient recipient
     * @return true if recipient is valid
     */
    public boolean validateContactPerson(ContactPerson contactPerson) {
        return validator.validate(contactPerson).size() == 0;
    }

    /**
     * Validates a request body against model.
     *
     * @param requestBody request body
     * @return true if request is valid
     */
    public boolean validateRequestBody(ContactRequestBody requestBody) {
        return validator.validate(requestBody).size() == 0;
    }

    /**
     * Lazy instance holder.
     */
    private static class Holder {
        private static final ContactValidator INSTANCE = new ContactValidator();
    }

    /**
     * Validates a request against model.
     *
     * @param request request
     * @return true if request is valid
     */
    public boolean validateRequest(ContactRequest request) {
        for (ContactPerson contactPerson : request.getContactPersons()) {
            if (!validateContactPerson(contactPerson)) {
                return false;
            }
        }
        return validator.validate(request).size() == 0 && validateRequestBody(request.getBody());
    }
}
