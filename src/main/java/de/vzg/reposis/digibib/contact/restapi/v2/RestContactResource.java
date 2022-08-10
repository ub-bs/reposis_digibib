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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestStateException;
import de.vzg.reposis.digibib.contact.model.ContactRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.tags.Tag;

import org.mycore.common.MCRException;
import org.mycore.restapi.annotations.MCRRequireTransaction;
import org.mycore.restapi.v2.access.MCRRestAPIACLPermission;
import org.mycore.restapi.v2.annotation.MCRRestRequiredPermission;

@Path("/contacts/")
public class RestContactResource {

    @GET
    @Operation(
        summary = "Lists all contact requests",
        responses = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = MediaType.APPLICATION_JSON,
                array = @ArraySchema(schema = @Schema(implementation = ContactRequest.class)))}),
            @ApiResponse(responseCode = "401",
                description = "You do not have create permission and need to authenticate first",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
        })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public List<ContactRequest> getContactRequests(@DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("128") @QueryParam("limit") int limit, @Context HttpServletResponse response) {
        final List<ContactRequest> contactRequests = ContactRequestService.getInstance().getContactRequests();
        response.setHeader("X-Total-Count", Integer.toString(contactRequests.size()));
        return contactRequests.stream().skip(offset).limit(limit).collect(Collectors.toList());
    }

    @GET
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_ID + "}")
    @Operation(
        summary = "Gets contact request by id",
        responses = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRequest.class))),
            @ApiResponse(responseCode = "401",
                description = "You do not have create permission and need to authenticate first",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
            @ApiResponse(responseCode = "404", description = "Request does not exist",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
        })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public ContactRequest getContactRequestByUUID(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_ID) UUID id)
            throws ContactRequestNotFoundException {
        final ContactRequest contactRequest = ContactRequestService.getInstance().getContactRequestByUUID(id);
        if (contactRequest != null) {
            return contactRequest;
        } else {
            throw new ContactRequestNotFoundException();
        }
    }

    @POST
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_ID + "}/forward")
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public Response forward(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_ID) UUID id)
            throws ContactRequestNotFoundException, ContactRequestStateException, MCRException {
        ContactRequestService.getInstance().forwardContactRequest(id);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{" + RestConstants.PARAM_CONTACT_REQUEST_ID + "}")
    @Operation(
        summary = "Deletes contact request by id",
        responses = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401",
                description = "You do not have create permission and need to authenticate first",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
            @ApiResponse(responseCode = "404", description = "Request does not exist",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON) }),
        })
    @MCRRequireTransaction
    public Response removeContactRequestByUUID(@PathParam(RestConstants.PARAM_CONTACT_REQUEST_ID) UUID id)
            throws ContactRequestNotFoundException {
        ContactRequestService.getInstance().removeContactRequestByUUID(id);
        return Response.noContent().build();
    }
}
