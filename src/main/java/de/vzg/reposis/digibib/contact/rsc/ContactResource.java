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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.ContactMailService;
import de.vzg.reposis.digibib.contact.ContactService;
import de.vzg.reposis.digibib.contact.ContactUtils;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.validation.ContactValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.restapi.annotations.MCRRequireTransaction;

@Path("/contact")
public class ContactResource {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Set<String> ALLOWED_GENRES = MCRConfiguration2
            .getString(ContactConstants.CONF_PREFIX + "Genres.Enabled").stream().flatMap(MCRConfiguration2::splitValue)
            .collect(Collectors.toSet());

    private static final String NEW_REQUEST_STYLESHEET = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "NewRequestMail.Stylesheet");

    private static final String RECEIPT_CONFIRMATION_STYLESHEET = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "ReceiptConfirmationMail.Stylesheet");

    private static final String FALLBACK_MAIL = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Mail");

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "contact involved persons of given object",
            responses = {
                    @ApiResponse(responseCode = "200", description = "operation was successful"),
                    @ApiResponse(responseCode = "404", description = "object is not found"), })
    @MCRRequireTransaction
    @ContactCheckCageCaptcha
    public Response save(ContactRequest request) throws Exception {
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
        ContactService.getInstance().insertRequest(request);
        final EMail confirmationMail = createConfirmationMail(request.getName(), request.getMessage(),
                request.getORCID(), objectID.toString());
        ContactMailService.sendMail(confirmationMail, request.getFrom());
        try {
            ContactMailService.sendMail(createNotificationMail(objectID.toString()), FALLBACK_MAIL);
        } catch (Exception e) {
            LOGGER.error("Cannot send notification mail", e);
        }
        return Response.ok().build();
    }

    private static EMail createNotificationMail(String id) throws Exception {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap();
        properties.put("id", id);
        final Element mailElement = ContactUtils
                .transform(baseMail.toXML(), NEW_REQUEST_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(mailElement);
    }

    private static EMail createConfirmationMail(String name, String message, String orcid, String id) throws Exception {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap();
        properties.put("id", id);
        properties.put("message", message);
        properties.put("name", name);
        if (orcid != null) {
            properties.put("name", orcid);
        }
        final Element mailElement = ContactUtils
                .transform(baseMail.toXML(), RECEIPT_CONFIRMATION_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(mailElement);
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
