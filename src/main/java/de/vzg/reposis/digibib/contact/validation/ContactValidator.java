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

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequest;

/**
 * This class provies a validator service.
 */
public class ContactValidator {

    private final Validator validator;

    private ContactValidator() {
       final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
       validator = factory.getValidator();
    }

    public static ContactValidator getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Validates a request against model.
     * @param request the request
     * @return true if request is valid
     */
    public boolean validateRequest(ContactRequest request) {
        return validator.validate(request).size() == 0;
    }

    /**
     * Validates a recipient against model.
     * @param recipient the recipient 
     * @return true if recipient is valid
     */
    public boolean validateRecipient(ContactRecipient recipient) {
        return validator.validate(recipient).size() == 0;
    }

    /**
     * Lazy instance holder.
     */
    private static class Holder {
        private static final ContactValidator INSTANCE = new ContactValidator();
    }
}
