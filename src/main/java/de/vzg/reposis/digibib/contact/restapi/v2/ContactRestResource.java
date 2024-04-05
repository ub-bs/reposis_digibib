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
import java.util.Optional;
import java.util.UUID;

import org.mycore.restapi.annotations.MCRRequireTransaction;
import org.mycore.restapi.v2.access.MCRRestAPIACLPermission;
import org.mycore.restapi.v2.annotation.MCRRestRequiredPermission;

import de.vzg.reposis.digibib.contact.ContactServiceImpl;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRequestData;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactRequestDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactRequestUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/contact-requests/")
public class ContactRestResource {

    @GET
    @Operation(summary = "Lists all contact requests", responses = { @ApiResponse(responseCode = "200", content = {
        @Content(mediaType = MediaType.APPLICATION_JSON,
            array = @ArraySchema(schema = @Schema(implementation = ContactRequestData.class))) }),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public List<ContactRequestDto> getAllRequests(@DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("128") @QueryParam("limit") int limit, @Context HttpServletResponse response) {
        final List<ContactRequest> requests = ContactServiceImpl.getInstance().listAllRequests();
        response.setHeader(ContactRestConstants.HEADER_TOTAL_COUNT, Integer.toString(requests.size()));
        return requests.stream().skip(offset).limit(limit).map(ContactRestHelper::toDto).toList();
    }

    @GET
    @Path("/{" + ContactRestConstants.PARAM_CONTACT_REQUEST_ID + "}")
    @Operation(summary = "Gets contact request by id", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRequestData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public ContactRequestDto getRequest(@PathParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId) {
        return Optional.of(ContactServiceImpl.getInstance().getRequest(requestId))
            .map(ContactRestHelper::toDto).get();
    }

    @PUT
    @Path("/{" + ContactRestConstants.PARAM_CONTACT_REQUEST_ID + "}")
    @Operation(summary = "Updates contact request by id", responses = { @ApiResponse(responseCode = "204"),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Consumes(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    public Response updateRequest(@PathParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        ContactRequestUpdateDto requestDto) {
        Optional.ofNullable(requestDto).map(ContactRestHelper::toDomain).ifPresentOrElse(r -> {
            r.setId(requestId);
            ContactServiceImpl.getInstance().updateRequest(r);
        }, () -> {
            throw new BadRequestException();
        });
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{" + ContactRestConstants.PARAM_CONTACT_REQUEST_ID + "}")
    @Operation(summary = "Deletes contact request by id", responses = { @ApiResponse(responseCode = "204"),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @MCRRequireTransaction
    public Response removeRequest(@PathParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId) {
        ContactServiceImpl.getInstance().deleteRequest(requestId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{" + ContactRestConstants.PARAM_CONTACT_REQUEST_ID + "}/forward")
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response forwardRequest(@PathParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        @QueryParam("recipient") String mail) {
        Optional.ofNullable(mail).ifPresentOrElse(
            r -> ContactServiceImpl.getInstance().forwardRequest(requestId, r), () -> {
                throw new BadRequestException("mail is required");
            });
        return Response.ok().build();
    }
}
