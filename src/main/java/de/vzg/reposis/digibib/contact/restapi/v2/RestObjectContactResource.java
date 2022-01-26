package de.vzg.reposis.digibib.contact.restapi.v2;

import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.ContactRequestDAO;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.InvalidMessageException;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.validation.ValidationHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.restapi.annotations.MCRRequireTransaction;
import org.mycore.restapi.v2.MCRRestAuthorizationFilter;
import org.mycore.restapi.v2.MCRRestUtils;
import org.mycore.restapi.v2.access.MCRRestAPIACLPermission;
import org.mycore.restapi.v2.annotation.MCRRestRequiredPermission;

@Path("/objects/{" + MCRRestAuthorizationFilter.PARAM_MCRID + "}/contacts")
@Tag(name = MCRRestUtils.TAG_MYCORE_OBJECT)
public class RestObjectContactResource {

    private static final Set<String> ALLOWED_GENRES = MCRConfiguration2
            .getString(ContactConstants.CONF_PREFIX + "Genres.Enabled").stream().flatMap(MCRConfiguration2::splitValue)
            .collect(Collectors.toSet());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "contact involved persons of {" + MCRRestAuthorizationFilter.PARAM_MCRID + "}",
            tags = MCRRestUtils.TAG_MYCORE_OBJECT, responses = {
                    @ApiResponse(responseCode = "200", description = "operation was successful"),
                    @ApiResponse(responseCode = "404", description = "object is not found"), })
    @MCRRestRequiredPermission(MCRRestAPIACLPermission.READ)
    @MCRRequireTransaction
    public Response save(@PathParam(MCRRestAuthorizationFilter.PARAM_MCRID) MCRObjectID objectID,
            ContactRequest contactRequest) throws InvalidMessageException, ContactException {
        if (!MCRMetadataManager.exists(objectID)) {
            throw new ContactException(objectID.toString() + " does not exist.");
        }
        contactRequest.setObjectID(objectID);
        if (!ValidationHelper.validateContactRequest(contactRequest)) {
            throw new InvalidMessageException();
        }
        String genre = null;
        try {
            genre = getGenre(objectID);
        } catch (MCRException e) {
            throw new ContactException("No genre for: " + objectID.toString()); // TODO check if wrapping is ness or
                                                                                // mcrexceptino mapper works
        }
        if (genre == null || !ALLOWED_GENRES.contains(genre)) {
            throw new ContactException("Not activated for genre: " + genre);
        }
        ContactRequestDAO.getInstance().save(contactRequest);
        return Response.ok().build();
    }

    private static String getGenre(MCRObjectID objectId) throws MCRException {
        final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectId);
        final MCRMODSWrapper wrapper = new MCRMODSWrapper(object);
        final Element genreElement = wrapper.getElement("mods:genre");
        if (genreElement != null) {
            final Attribute valueURIAttribute = genreElement.getAttribute("valueURI");
            if (valueURIAttribute != null) {
                final String valueURI = valueURIAttribute.getValue();
                return valueURI.substring(valueURI.lastIndexOf("#") + 1);
            }
        }
        throw new MCRException("Object has no genre.");
    }
}
