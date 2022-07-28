package de.vzg.reposis.digibib.contact.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import de.vzg.reposis.digibib.contact.model.ContactRequest;

public class ValidationHelper {

    public static boolean validateContactRequest(ContactRequest contactRequest) {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        final Set<ConstraintViolation<ContactRequest>> constraintViolations = validator.validate(contactRequest);
        return constraintViolations.size() == 0;
    }
}
