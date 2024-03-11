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
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.restapi.annotations.MCRRequireTransaction;

import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.ContactServiceImpl;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.validation.ContactValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/contact")
public class ContactResource {

    private static final Set<String> ALLOWED_GENRES = MCRConfiguration2
        .getString(ContactConstants.CONF_PREFIX + "Genres.Enabled").stream()
        .flatMap(MCRConfiguration2::splitValue).collect(Collectors.toSet());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "contact involved persons of given object", responses = {
        @ApiResponse(responseCode = "200", description = "operation was successful"),
        @ApiResponse(responseCode = "404", description = "object is not found"), })
    @MCRRequireTransaction
    @ContactCheckCageCaptcha
    public Response save(ContactRequest request) throws Exception {
        if (!ContactValidator.getInstance().validateRequest(request)) {
            throw new BadRequestException("invalid request");
        }
        final MCRObjectID objectId = request.getObjectId();
        if (!MCRMetadataManager.exists(objectId)) {
            throw new BadRequestException(objectId.toString() + " does not exist");
        }
        String genre = null;
        try {
            genre = getGenre(objectId);
        } catch (MCRException e) {
            throw new BadRequestException("No genre for: " + objectId.toString());
        }
        if (genre == null || !ALLOWED_GENRES.contains(genre)) {
            throw new BadRequestException("Not activated for genre: " + genre);
        }
        ContactServiceImpl.getInstance().addRequest(request);
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
