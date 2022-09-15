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

import de.vzg.reposis.digibib.contact.ContactService;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.ContactRecipientNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
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
    public List<ContactRecipient> getAllRecipientsByRequestUUID(@DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("128") @QueryParam("limit") int limit, @Context HttpServletResponse response,
            @QueryParam(RestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestUUID) throws ContactRequestNotFoundException {
        final ContactRequest request = ContactService.getInstance().getRequestByUUID(requestUUID);
        if (request == null) {
            throw new ContactRequestNotFoundException();
        }
        final List<ContactRecipient> recipients = request.getRecipients();
        response.setHeader("X-Total-Count", Integer.toString(recipients.size()));
        return recipients.stream().skip(offset).limit(limit).collect(Collectors.toList());
    }

    @POST
    @Operation(
        summary = "Creates new contact request recipient",
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
        ContactService.getInstance().addRecipient(requestUUID, recipient);
        return Response.created(info.getAbsolutePath().resolve(String.format("recipients/%s", recipient.getEmail()))).build();
    }

    @GET
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID + "}")
    @Operation(
        summary = "Gets contact request recipient by request uuid and mail",
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
    public ContactRecipient getRecipientByMail(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) UUID requestUUID,
            @PathParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) String mail) throws ContactRequestNotFoundException { // TODO
        final ContactRequest request = ContactService.getInstance().getRequestByUUID(requestUUID);
        if (request == null) {
            throw new ContactRequestNotFoundException();
        }
        return request.getRecipients().stream().filter(r -> mail.equals(r.getEmail())).findFirst()
                .orElseThrow(() -> new ContactRecipientNotFoundException());
    }

    @PUT
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID + "}")
    @Operation(
        summary = "Updates contact request recipient by mail",
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
    public Response updateRecipientByMail(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestUUID,
            @PathParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) String mail, // TODO
            ContactRecipient recipient) throws ContactException {
        ContactService.getInstance().updateRecipientByMail(requestUUID, mail, recipient);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID + "}")
    @Operation(
        summary = "Deletes contact request recipient by mail",
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
    public Response removeRecipientByMail(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestUUID,
            @PathParam(RestConstants.PARAM_CONTACT_REQUEST_RECIPIENT_ID) String mail) throws ContactException { // TODO
        ContactService.getInstance().removeRecipientByMail(requestUUID, mail);
        return Response.noContent().build();
    }
}
