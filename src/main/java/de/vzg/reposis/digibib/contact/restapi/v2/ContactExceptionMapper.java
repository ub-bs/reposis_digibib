package de.vzg.reposis.digibib.contact.restapi.v2;

import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import de.vzg.reposis.digibib.contact.exception.ContactException;

import org.mycore.restapi.v2.MCRErrorResponse;

@Provider
public class ContactExceptionMapper implements ExceptionMapper<ContactException> {

    @Context
    Request request;

    @Override
    public Response toResponse(ContactException exception) {
        return getResponse(exception, Response.Status.BAD_REQUEST.getStatusCode(), exception.getErrorCode());
    }

    // TODO use rest api function
    private static Response getResponse(Exception e, int statusCode, String errorCode) {
        MCRErrorResponse response = MCRErrorResponse.fromStatus(statusCode).withCause(e).withMessage(e.getMessage())
                .withDetail(Optional.of(e).map(ex -> (ex instanceof WebApplicationException) ? ex.getCause() : ex)
                        .map(Object::getClass).map(Class::getName).orElse(null))
                .withErrorCode(errorCode);
        // LogManager.getLogger().error(response::getLogMessage, e);
        return Response.status(response.getStatus()).entity(response).build();
    }
}
