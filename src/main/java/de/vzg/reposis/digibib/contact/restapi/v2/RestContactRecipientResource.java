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

package de.vzg.reposis.digibib.contact.restapi.v2;

import java.util.List;
import java.util.UUID;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.tags.Tag;

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientOriginException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;
import de.vzg.reposis.digibib.contact.model.ContactRequest;

import org.mycore.restapi.annotations.MCRRequireTransaction;
import org.mycore.restapi.v2.access.MCRRestAPIACLPermission;
import org.mycore.restapi.v2.annotation.MCRRestRequiredPermission;

@Path("contacts/{" + RestConstants.PARAM_CONTACT_REQUEST_ID + "}/recipients")
public class RestContactRecipientResource {

    @Context
    private UriInfo info;

    @GET
    @Operation(
        summary = "Gets contact request recipients by request uuid",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipient.class))),
            @ApiResponse(responseCode = "401",
                description = "You do not have create permission and need to authenticate first",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
            @ApiResponse(responseCode = "404", description = "Request does not exist",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
        })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public List<ContactRecipient> getRecipientsByRequestUUID(@DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("128") @QueryParam("limit") int limit, @Context HttpServletResponse response,
            @QueryParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) UUID requestUUID) throws ContactRequestNotFoundException {
        final List<ContactRecipient> recipients = ContactRequestService.getInstance().listRecipientsByUUID(requestUUID);
        response.setHeader("X-Total-Count", Integer.toString(recipients.size()));
        return recipients.stream().skip(offset).limit(limit).collect(Collectors.toList());
    }

    @POST
    @Operation(
        summary = "Creates new contact request recipient for id",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipient.class))),
            @ApiResponse(responseCode = "401",
                description = "You do not have create permission and need to authenticate first",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
            @ApiResponse(responseCode = "404", description = "Request does not exist",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
        })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response addRecipient(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestUUID,
            ContactRecipient recipient) throws ContactException {
        if (!ContactRecipientOrigin.MANUAL.equals(recipient.getOrigin())) {
            throw new ContactRecipientOriginException();
        }
        final ContactRequestService service = ContactRequestService.getInstance();
        final ContactRequest request = service.getRequestByUUID(requestUUID);
        if (request == null) {
            throw new ContactRequestNotFoundException();
        }
        service.addRecipient(request.getId(), recipient);
        return Response.created(info.getAbsolutePath().resolve(String.format("recipients/%s", recipient.getUuid().toString()))).build();
    }

    @GET
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID + "}")
    @Operation(
        summary = "Gets contact request recipient by request uuid",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipient.class))),
            @ApiResponse(responseCode = "401",
                description = "You do not have create permission and need to authenticate first",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
            @ApiResponse(responseCode = "404", description = "Request does not exist",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
        })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public ContactRecipient getRecipientByUUID(@QueryParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) UUID requestUUID,
            @PathParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) UUID recipientUUID) throws ContactRequestNotFoundException {
        final ContactRequest request = ContactRequestService.getInstance().getRequestByUUID(requestUUID);
        if (request == null) {
            throw new ContactRequestNotFoundException();
        }
        return request.getRecipients().stream().filter(r -> recipientUUID.equals(r.getUuid())).findFirst()
                .orElseThrow(() -> new ContactRecipientNotFoundException());
    }

    @PUT
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID + "}")
    @Operation(
        summary = "Updates contact request recipient by id",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipient.class))),
            @ApiResponse(responseCode = "401",
                description = "You do not have create permission and need to authenticate first",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
            @ApiResponse(responseCode = "404", description = "Request does not exist",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
        })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response updateRecipientByUUID(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestUUID,
            @PathParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) UUID recipientUUID,
            ContactRecipient recipient) throws ContactException {
        final ContactRequestService service = ContactRequestService.getInstance();
        final ContactRecipient outdated = service.getRecipientByUUID(recipientUUID);
        if (outdated == null) {
            throw new ContactRecipientNotFoundException();
        }
        if (!ContactRecipientOrigin.MANUAL.equals(outdated.getOrigin())
                  && (!Objects.equals(outdated.getName(), recipient.getName())
                  || !Objects.equals(outdated.getOrigin(), recipient.getOrigin())
                  || !Objects.equals(outdated.getEmail(), recipient.getEmail()))) {
            throw new ContactRecipientOriginException();
        }
        recipient.setId(outdated.getId());
        recipient.setRequest(outdated.getRequest());
        service.updateRecipient(recipient);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID + "}")
    @Operation(
        summary = "Deletes contact request recipient by id",
        responses = {
            @ApiResponse(responseCode = "201", content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipient.class))),
            @ApiResponse(responseCode = "401",
                description = "You do not have create permission and need to authenticate first",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
            @ApiResponse(responseCode = "404", description = "Request does not exist",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
        })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response removeRecipient(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) UUID recipientUUID)
            throws ContactException {
        final ContactRequestService service = ContactRequestService.getInstance();
        final ContactRecipient recipient = service.getRecipientByUUID(recipientUUID);
        if (recipient == null) {
            throw new ContactRecipientNotFoundException();
        }
        if (!ContactRecipientOrigin.MANUAL.equals(recipient.getOrigin())) {
            throw new ContactRecipientOriginException();
        }
        service.removeRecipient(recipient);
        return Response.noContent().build();
    }
}
