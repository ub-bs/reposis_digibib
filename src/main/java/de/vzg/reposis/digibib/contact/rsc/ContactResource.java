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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.restapi.annotations.MCRRequireTransaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.ContactServiceImpl;
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.restapi.v2.ContactRestConstants;
import de.vzg.reposis.digibib.contact.validation.ContactValidator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/contact")
public class ContactResource {

    private static final String PARAM_OBJECT_ID = "object_id";

    private static final String PARAM_REQUEST_ID = "request_id";

    private static final Set<String> ALLOWED_GENRES
        = MCRConfiguration2.getString(ContactConstants.CONF_PREFIX + "Genres.Enabled").stream()
            .flatMap(MCRConfiguration2::splitValue).collect(Collectors.toSet());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    @ContactCheckCageCaptcha
    @Path("/new-request/{" + PARAM_OBJECT_ID + "}/")
    public Response createRequest(@PathParam(PARAM_OBJECT_ID) String objectIdString,
        ContactRequestCreateDto requestBodyDto) {
        final ContactRequestBody requestBody = toRequestBody(requestBodyDto);
        if (!ContactValidator.getInstance().validateRequestBody(requestBody)) {
            throw new BadRequestException("invalid request");
        }
        final MCRObjectID objectId = Optional.of(objectIdString).filter(MCRObjectID::isValid)
            .map(MCRObjectID::getInstance).orElseThrow(() -> new BadRequestException("object is invalid"));
        if (!MCRMetadataManager.exists(objectId)) {
            throw new BadRequestException("object does not exist");
        }
        ensureGenreIsEnabled(objectId);
        ContactServiceImpl.getInstance().createRequest(objectId, requestBody);
        return Response.ok().build();
    }

    @MCRRequireTransaction
    @Path("/confirm-request/{" + PARAM_REQUEST_ID + "}/")
    public Response confirmRequest(@PathParam(PARAM_REQUEST_ID) UUID requestId, @QueryParam("recipient") String mail) {
        Optional.ofNullable(mail)
            .ifPresentOrElse(r -> ContactServiceImpl.getInstance().confirmForwarding(requestId, r), () -> {
                throw new BadRequestException();
            });
        return Response.ok().build();
    }

    @GET
    @Path("request-status/{" + ContactRestConstants.PARAM_CONTACT_REQUEST_ID + "}/")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRequestStatus(@PathParam(ContactRestConstants.PARAM_CONTACT_REQUEST_ID) UUID requestId) {
        final ContactRequest request = ContactServiceImpl.getInstance().getRequest(requestId);
        if (Objects.equals(ContactRequest.State.CONFIRMED, request.getState())) {
            final List<ContactPerson> contactPersons = request.getContactPersons().stream()
                .filter(p -> p.getForwarding() != null).filter(p -> p.getForwarding().getConfirmed() != null).toList();
            String result = "";
            for (ContactPerson contactPerson : contactPersons) {
                result += String.format("CONFIRMED by: %s\n", contactPerson.getName());
            }
            return result;
        }
        return request.getState().toString();
    }

    private static ContactRequestBody toRequestBody(ContactRequestCreateDto requestBodyDto) {
        return new ContactRequestBody(requestBodyDto.name(), requestBodyDto.email(), requestBodyDto.orcid(),
            requestBodyDto.message());
    }

    private void ensureGenreIsEnabled(MCRObjectID objectId) {
        String genre = null;
        try {
            genre = getGenre(objectId);
        } catch (MCRException e) {
            throw new BadRequestException("No genre for: " + objectId.toString());
        }
        if (!ALLOWED_GENRES.contains(genre)) {
            throw new BadRequestException("Not activated for genre: " + genre);
        }
    }

    private static String getGenre(MCRObjectID objectId) {
        return Optional.of(MCRMetadataManager.retrieveMCRObject(objectId)).map(o -> new MCRMODSWrapper(o))
            .map(w -> w.getElement("mods:genre")).map(e -> e.getAttribute("valueURI"))
            .map(Attribute::getValue).map(v -> v.substring(v.lastIndexOf("#") + 1))
            .orElseThrow(() -> new MCRException("Object has no genre."));
    }

    private record ContactRequestCreateDto(@JsonProperty("name") String name, @JsonProperty("email") String email,
        @JsonProperty("orcid") String orcid, @JsonProperty("message") String message) {

        @SuppressWarnings("unused")
        public ContactRequestCreateDto(String name, String email, String message) {
            this(name, email, null, message);
        }
    }
}
