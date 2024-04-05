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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJAXBContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.restapi.annotations.MCRRequireTransaction;
import org.mycore.tools.MyCoReWebPageProvider;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.vzg.reposis.digibib.captcha.cage.rsc.filter.ContactCheckCageCaptcha;
import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.ContactServiceImpl;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.model.ContactPerson;
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
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@Path("/contact-request")
public class ContactResource {

    private static final String QUERY_PARAM_REQUEST_ID = "rid";

    private static final String QUERY_PARAM_MAIL = "m";

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

    // TODO switch to POST
    @GET
    @MCRRequireTransaction
    @Path("confirm")
    public Response confirmRequest(@QueryParam(QUERY_PARAM_REQUEST_ID) UUID requestId,
        @QueryParam(QUERY_PARAM_MAIL) String mail) {
        if (requestId == null || mail == null) {
            throw new BadRequestException();
        }
        ContactServiceImpl.getInstance().confirmForwarding(requestId, mail);
        return Response.ok().build();
    }

    @GET
    @Path("status")
    @Produces(MediaType.TEXT_HTML)
    public InputStream getRequestStatus(@QueryParam(QUERY_PARAM_REQUEST_ID) UUID requestId) throws IOException {
        if (requestId == null) {
            MCRJerseyUtil.throwException(Response.Status.BAD_REQUEST, "request id is required");
        }
        ContactRequest request = null;
        try {
            request = ContactServiceImpl.getInstance().getRequest(requestId);
        } catch (ContactRequestNotFoundException e) {
            MCRJerseyUtil.throwException(Response.Status.NOT_FOUND, "request does not exist");
        }
        final ContactStatus status = toContactStatus(request);
        final MCRContent content = marshalContactStatus(status);
        try {
            final MCRContent section = MCRJerseyUtil.transform(content.asXML(), req);
            final MyCoReWebPageProvider result = new MyCoReWebPageProvider();
            result.addSection("", section.asXML().detachRootElement(),
                MCRSessionMgr.getCurrentSession().getCurrentLanguage());
            return MCRLayoutService.instance().getTransformedContent(req, res, new MCRJDOMContent(result.getXML()))
                .getInputStream();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private ContactStatus toContactStatus(ContactRequest request) {
        final ContactStatus status = new ContactStatus();
        if (request.getState().getValue() >= ContactRequest.State.FORWARDED.getValue()) {
            final List<String> emails = request.getContactPersons().stream().map(ContactPerson::getMail)
                .map(ContactResource::maskMailAdress).toList();
            status.setEmails(emails);
        }
        status.setStatus(request.getState().toString().toLowerCase());
        return status;
    }

    private static MCRContent marshalContactStatus(ContactStatus contactStatus) throws IOException {
        try {
            return new MCRJAXBContent<>(JAXBContext.newInstance(ContactStatus.class), contactStatus);
        } catch (JAXBException e) {
            throw new IOException("Invalid input");
        }
    }

    // https://stackoverflow.com/questions/43003138/regular-expression-for-email-masking
    private static String maskMailAdress(String mail) {
        return mail.replaceAll("(?<=.)[^@](?=[^@]*[^@]@)|(?:(?<=@.)|(?!^)\\G(?=[^@]*$)).(?!$)", "*");
    }

    @XmlRootElement(name = "ContactStatus")
    private static class ContactStatus {

        @XmlElement(name = "status")
        private String status;

        @XmlElement(name = "emails")
        private List<String> emails;

        public void setStatus(String status) {
            this.status = status;
        }

        public void setEmails(List<String> emails) {
            this.emails = emails;
        }
    }

    private record ContactRequestCreateDto(@JsonProperty("objectId") String objectId, @JsonProperty("name") String name,
        @JsonProperty("email") String email, @JsonProperty("orcid") String orcid,
        @JsonProperty("message") String message) {

        @SuppressWarnings("unused")
        public ContactRequestCreateDto(String objectId, String name, String email, String message) {
            this(objectId, name, email, null, message);
        }
    }
}
