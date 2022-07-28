package de.vzg.reposis.digibib.contact.restapi.v2;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.model.ContactRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.tags.Tag;

import org.mycore.restapi.v2.access.MCRRestAPIACLPermission;
import org.mycore.restapi.v2.annotation.MCRRestRequiredPermission;

@Path("/contacts/")
public class RestContactResource {

    protected final String PARAM_CONTACT_REQUEST_ID = "contact_request_id";

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
    public List<ContactRequest> findAll(@DefaultValue("0") @QueryParam("offset") int offset,
        @DefaultValue("128") @QueryParam("limit") int limit) {
        return ContactRequestService.getInstance().getContactRequests().stream().skip(offset).limit(limit)
                .collect(Collectors.toList()); // TODO send size header
    }

    @GET
    @Path("/{" + PARAM_CONTACT_REQUEST_ID + "}")
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
    public ContactRequest findAllByID(@PathParam(PARAM_CONTACT_REQUEST_ID) long id) {
        return ContactRequestService.getInstance().getContactRequestByID(id);
    }

    @POST
    @Path("/{" + PARAM_CONTACT_REQUEST_ID + "}/execute")
    public Response process(@PathParam(PARAM_CONTACT_REQUEST_ID) long id) {
        return null; // TODO
    }
}
