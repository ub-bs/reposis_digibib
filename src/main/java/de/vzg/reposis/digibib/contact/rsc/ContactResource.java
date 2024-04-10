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

import java.util.Date;
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
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.restapi.annotations.MCRRequireTransaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.captcha.cage.rsc.filter.ContactCheckCageCaptcha;
import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.ContactServiceImpl;
import de.vzg.reposis.digibib.contact.exception.ContactPersonNotFoundException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactPersonEvent;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import de.vzg.reposis.digibib.contact.validation.ContactValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/contact-request")
public class ContactResource {

    private static final String QUERY_PARAM_REQUEST_ID = "rid";

    private static final String QUERY_PARAM_PERSON_MAIL = "m";

    private static final Set<String> ALLOWED_GENRES
        = MCRConfiguration2.getString(ContactConstants.CONF_PREFIX + "Genres.Enabled").stream()
            .flatMap(MCRConfiguration2::splitValue).collect(Collectors.toSet());

    @Context
    HttpServletRequest req;

    @Context
    HttpServletResponse res;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRequireTransaction
    @ContactCheckCageCaptcha
    @Path("create")
    public Response createRequest(ContactRequestCreateDto requestBodyDto) {
        final ContactRequestBody requestBody = toRequestBody(requestBodyDto);
        if (!ContactValidator.getInstance().validateRequestBody(requestBody)) {
            throw new BadRequestException("invalid request");
        }
        final MCRObjectID objectId = Optional.ofNullable(requestBodyDto.objectId).filter(MCRObjectID::isValid)
            .map(MCRObjectID::getInstance).orElseThrow(() -> new BadRequestException("object is missing or invalid"));
        if (!MCRMetadataManager.exists(objectId)) {
            throw new BadRequestException("object does not exist");
        }
        ensureGenreIsEnabled(objectId);
        ContactServiceImpl.getInstance().createRequest(objectId, requestBody);
        return Response.ok().build();
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

    @POST
    @MCRRequireTransaction
    @Path("answer")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response answerRequest(@QueryParam(QUERY_PARAM_REQUEST_ID) UUID requestId,
        @QueryParam(QUERY_PARAM_PERSON_MAIL) String mail, ContactPersonAnswerDto answer) {
        if (requestId == null || mail == null) {
            throw new BadRequestException();
        }
        final ContactPersonEvent event
            = new ContactPersonEvent(new Date(), ContactPersonEvent.EventType.CONFIRMED, answer.comment);
        try {
            ContactServiceImpl.getInstance().addPersonEvent(requestId, mail, event);
        } catch (ContactRequestNotFoundException | ContactPersonNotFoundException e) {
            throw new BadRequestException();
        }
        return Response.ok().build();
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public ContactRequestStatusDto getRequestStatus(@QueryParam(QUERY_PARAM_REQUEST_ID) UUID requestId) {
        if (requestId == null) {
            MCRJerseyUtil.throwException(Response.Status.BAD_REQUEST, "request id is required");
        }
        ContactRequest request = null;
        try {
            request = ContactServiceImpl.getInstance().getRequest(requestId);
        } catch (ContactRequestNotFoundException e) {
            MCRJerseyUtil.throwException(Response.Status.NOT_FOUND, "request does not exist");
        }
        return toContactStatus(request);
    }

    private ContactRequestStatusDto toContactStatus(ContactRequest request) {
        final List<String> emails = request.getContactPersons().stream().filter(c -> {
            return c.getEvents().stream().anyMatch(e -> Objects.equals(ContactPersonEvent.EventType.SENT, e.type()));
        }).map(ContactPerson::getMail).map(ContactResource::maskMailAdress).toList();
        final String status = request.getState().toString().toLowerCase();
        return new ContactRequestStatusDto(status, emails);
    }

    // https://stackoverflow.com/questions/43003138/regular-expression-for-email-masking
    private static String maskMailAdress(String mail) {
        return mail.replaceAll("(?<=.)[^@](?=[^@]*[^@]@)|(?:(?<=@.)|(?!^)\\G(?=[^@]*$)).(?!$)", "*");
    }

    private record ContactRequestStatusDto(@JsonProperty("status") String status,
        @JsonProperty("emails") List<String> mails) {
    }

    private record ContactRequestCreateDto(@JsonProperty("objectId") String objectId, @JsonProperty("name") String name,
        @JsonProperty("email") String email, @JsonProperty("orcid") String orcid,
        @JsonProperty("message") String message) {

        @SuppressWarnings("unused")
        public ContactRequestCreateDto(String objectId, String name, String email, String message) {
            this(objectId, name, email, null, message);
        }
    }

    private record ContactPersonAnswerDto(@JsonProperty("comment") String comment) {
    }
}
