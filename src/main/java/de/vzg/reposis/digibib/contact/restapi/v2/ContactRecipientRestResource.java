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

import org.mycore.restapi.annotations.MCRRequireTransaction;
import org.mycore.restapi.v2.access.MCRRestAPIACLPermission;
import org.mycore.restapi.v2.annotation.MCRRestRequiredPermission;

import de.vzg.reposis.digibib.contact.ContactServiceImpl;
import de.vzg.reposis.digibib.contact.model.Contact;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.persistence.model.ContactData;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactCreateDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactDto;
import de.vzg.reposis.digibib.contact.restapi.v2.dto.ContactUpdateDto;
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

@Path("contact-requests/{" + ContactRestResource.PARAM_CONTACT_REQUEST_ID + "}/recipients")
public class ContactRecipientRestResource {

    public static final String PARAM_CONTACT_EMAIL = "contact_request_recipient_id";

    @Context
    private UriInfo info;

    @POST
    @Operation(summary = "Creates and adds contact to request", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response createContact(@PathParam(ContactRestResource.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        ContactCreateDto contactDto) {
        final Contact contactPerson = ContactRestHelper.toDomain(contactDto);
        ContactServiceImpl.getInstance().addContact(requestId, contactPerson);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Gets contacts from request", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public List<ContactDto> getContacts(@DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("128") @QueryParam("limit") int limit, @Context HttpServletResponse response,
        @QueryParam(ContactRestResource.PARAM_CONTACT_REQUEST_ID) UUID requestId) {
        final ContactRequest request = ContactServiceImpl.getInstance().getRequest(requestId);
        final List<Contact> contacts = request.getContacts();
        response.setHeader(ContactRestConstants.HEADER_TOTAL_COUNT, Integer.toString(contacts.size()));
        return contacts.stream().skip(offset).limit(limit).map(ContactRestHelper::toDto).toList();
    }

    @GET
    @Path("/{" + PARAM_CONTACT_EMAIL + "}")
    @Operation(summary = "Gets contact from request by email", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request/contact does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    public ContactDto getContact(@PathParam(ContactRestResource.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        @PathParam(PARAM_CONTACT_EMAIL) String emailAddress) {
        return ContactRestHelper.toDto(ContactServiceImpl.getInstance().getContactByEmail(requestId, emailAddress));
    }

    @PUT
    @Path("/{" + PARAM_CONTACT_EMAIL + "}")
    @Operation(summary = "Updates contact from request by email", responses = {
        @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request/contact does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response updateContact(@PathParam(ContactRestResource.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        @PathParam(PARAM_CONTACT_EMAIL) String mail, ContactUpdateDto contactDto) {
        if (contactDto == null) {
            throw new BadRequestException();
        }
        final Contact person = ContactRestHelper.toDomain(contactDto);
        ContactServiceImpl.getInstance().updateContact(requestId, person);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{" + PARAM_CONTACT_EMAIL + "}")
    @Operation(summary = "Deletes contact by email from request", responses = {
        @ApiResponse(responseCode = "201",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ContactData.class))),
        @ApiResponse(responseCode = "401",
            description = "You do not have create permission and need to authenticate first", content = {
                @Content(mediaType = MediaType.APPLICATION_JSON) }),
        @ApiResponse(responseCode = "404", description = "Request/contact does not exist", content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), })
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.DELETE)
    @MCRRequireTransaction
    public Response removeContact(@PathParam(ContactRestResource.PARAM_CONTACT_REQUEST_ID) UUID requestId,
        @PathParam(PARAM_CONTACT_EMAIL) String emailAddress) {
        ContactServiceImpl.getInstance().deleteContactByEmail(requestId, emailAddress);
        return Response.noContent().build();
    }
}
