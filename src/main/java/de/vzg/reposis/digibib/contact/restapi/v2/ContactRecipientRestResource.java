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
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.persistence.model.ContactRecipientData;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactPersonCreateDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactPersonDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactPersonUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
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
import jakarta.ws.rs.core.UriInfo;

@Path("contact-requests/{" + ContactRestConstants.PARAM_CONTACT_REQUEST_ID + "}/recipients")
public class ContactRecipientRestResource {

    @Context
    private UriInfo info;

    @POST
    @Operation(summary = "Creates new contact request recipient", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipientData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response createContactPerson(@PathParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        ContactPersonCreateDto contactPersonDto) {
        final ContactPerson contactPerson = ContactRestHelper.toDomain(contactPersonDto);
        ContactServiceImpl.getInstance().addContactPerson(requestId, contactPerson);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Gets contact request recipients by request id", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipientData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public List<ContactPersonDto> getAllRecipients(@DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("128") @QueryParam("limit") int limit, @Context HttpServletResponse response,
        @QueryParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId) {
        final ContactRequest request = ContactServiceImpl.getInstance().getRequest(requestId);
        final List<ContactPerson> recipients = request.getContactPersons();
        response.setHeader(ContactRestConstants.HEADER_TOTAL_COUNT, Integer.toString(recipients.size()));
        return recipients.stream().skip(offset).limit(limit).map(ContactRestHelper::toDto).toList();
    }

    @GET
    @Path("/{" + ContactRestConstants.PARAM_CONTACT_RECIPIENT_MAIL + "}")
    @Operation(summary = "Gets contact request recipient by request id", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipientData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public ContactPersonDto getRecipient(
        @PathParam(ContactRestConstants.PARAM_CONTACT_RECIPIENT_MAIL) UUID requestId,
        @PathParam(ContactRestConstants.PARAM_CONTACT_RECIPIENT_MAIL) String mail) {
        return ContactRestHelper.toDto(ContactServiceImpl.getInstance().getContactPerson(requestId, mail));
    }

    @PUT
    @Path("/{" + ContactRestConstants.PARAM_CONTACT_RECIPIENT_MAIL + "}")
    @Operation(summary = "Updates contact request recipient by id", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipientData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response updateRecipient(@PathParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        @PathParam(ContactRestConstants.PARAM_CONTACT_RECIPIENT_MAIL) String mail,
        ContactPersonUpdateDto recipientDto) {
        Optional.ofNullable(recipientDto).map(ContactRestHelper::toDomain).ifPresentOrElse(r -> {
            ContactServiceImpl.getInstance().updateContactPerson(requestId, r);
        }, () -> {
            throw new BadRequestException();
        });
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{" + ContactRestConstants.PARAM_CONTACT_RECIPIENT_MAIL + "}")
    @Operation(summary = "Deletes contact request recipient by id", responses = {
        @ApiResponse(responseCode = "201",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactRecipientData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response removeRecipient(@PathParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        @PathParam(ContactRestConstants.PARAM_CONTACT_RECIPIENT_MAIL) String mail) {
        ContactServiceImpl.getInstance().deleteContactPerson(requestId, mail);
        return Response.noContent().build();
    }
}
