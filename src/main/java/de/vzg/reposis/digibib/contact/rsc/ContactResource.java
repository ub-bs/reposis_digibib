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

package de.vzg.reposis.digibib.contact.rsc;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.validation.ContactValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.restapi.annotations.MCRRequireTransaction;

@Path("/contact")
public class ContactResource {

    private static final Set<String> ALLOWED_GENRES = MCRConfiguration2
            .getString(ContactConstants.CONF_PREFIX + "Genres.Enabled").stream().flatMap(MCRConfiguration2::splitValue)
            .collect(Collectors.toSet());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "contact involved persons of given object",
            responses = {
                    @ApiResponse(responseCode = "200", description = "operation was successful"),
                    @ApiResponse(responseCode = "404", description = "object is not found"), })
    @MCRRequireTransaction
    @ContactCheckCageCaptcha
    public Response save(ContactRequest request) throws BadRequestException {
        if (!ContactValidator.getInstance().validateRequest(request)) {
            throw new BadRequestException("invalid request");
        }
        final MCRObjectID objectID = request.getObjectID();
        if (!MCRMetadataManager.exists(objectID)) {
            throw new BadRequestException(objectID.toString() + " does not exist");
        }
        String genre = null;
        try {
            genre = getGenre(objectID);
        } catch (MCRException e) {
            throw new BadRequestException("No genre for: " + objectID.toString());
        }
        if (genre == null || !ALLOWED_GENRES.contains(genre)) {
            throw new BadRequestException("Not activated for genre: " + genre);
        }
        ContactRequestService.getInstance().insertRequest(request);
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
