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

package de.vzg.reposis.digibib.contact;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSL2XMLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.xml.sax.SAXException;

import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequest;

public class ContactServiceHelper {

    private static final String MAIL_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "RequestMail.Stylesheet");

    private static final String FORWARDING_CONFIRMATION_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "ForwardingConfirmationMail.Stylesheet");

    private static final String NEW_REQUEST_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "NewRequestMail.Stylesheet");

    private static final String RECEIPT_CONFIRMATION_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "ReceiptConfirmationMail.Stylesheet");

    private static final String FALLBACK_MAIL = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Mail");

    /**
     * Creates Email for recipient and sends it.
     *
     * @param request request
     * @param recipient recipient
     */
    protected static void sendRequestToRecipient(ContactRequest request, ContactRecipient recipient) {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put(ContactConstants.REQUEST_HEADER_NAME, request.getId().toString());
        final EMail mail = createMail(request, recipient);
        ContactMailService.sendMail(mail, recipient.getMail(), headers);
    }

    protected static void sendRequestForwardedMail(ContactRequest request) {
        final EMail mail = ContactServiceHelper.createForwardConfirmationMail(request);
        ContactMailService.sendMail(mail, request.getFrom());
    }

    protected static void sendConfirmationMail(ContactRequest request) {
        final EMail mail = createConfirmationMail(request);
        ContactMailService.sendMail(mail, request.getFrom());
    }

    protected static void sendNewRequestMail(ContactRequest request) {
        final EMail mail = createNotificationMail(request.getObjectId().toString());
        ContactMailService.sendMail(mail, FALLBACK_MAIL);
    }

    private static EMail createConfirmationMail(ContactRequest request) {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", request.getObjectId().toString());
        properties.put("message", request.getMessage());
        properties.put("name", request.getName());
        Optional.ofNullable(request.getOrcid()).ifPresent(o -> properties.put("orcid", o));
        final Element mailElement = transform(baseMail.toXML(), RECEIPT_CONFIRMATION_STYLESHEET, properties)
            .getRootElement();
        return EMail.parseXML(mailElement);
    }

    private static EMail createMail(ContactRequest request, ContactRecipient recipient) {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("email", request.getFrom());
        properties.put("id", request.getObjectId().toString());
        properties.put("message", request.getMessage());
        properties.put("name", request.getName());
        properties.put("recipient", recipient.getName());
        properties.put("recipientID", recipient.getId().toString());
        properties.put("requestID", request.getId().toString());
        properties.put("title", request.getObjectId().toString());
        Optional.ofNullable(request.getOrcid()).ifPresent(o -> properties.put("orcid", o));
        Optional.ofNullable(request.getComment()).ifPresent(c -> properties.put("comment", c));

        final Element mailElement = transform(baseMail.toXML(), MAIL_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(mailElement);
    }

    private static EMail createForwardConfirmationMail(ContactRequest request) {
        final EMail forwardConfirmation = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", request.getObjectId().toString());
        properties.put("name", request.getName());
        final Element mailElement = transform(forwardConfirmation.toXML(), FORWARDING_CONFIRMATION_STYLESHEET,
            properties).getRootElement();
        return EMail.parseXML(mailElement);
    }

    private static Document transform(Document input, String stylesheet, Map<String, String> parameters) {
        MCRJDOMContent source = new MCRJDOMContent(input);
        MCRXSL2XMLTransformer transformer = MCRXSL2XMLTransformer.getInstance("xsl/" + stylesheet + ".xsl");
        MCRParameterCollector parameterCollector = MCRParameterCollector.getInstanceFromUserSession();
        parameterCollector.setParameters(parameters);
        try {
            return transformer.transform(source, parameterCollector).asXML();
        } catch (IOException | JDOMException | SAXException e) {
            throw new ContactException("Cannot transform document", e);
        }
    }

    private static EMail createNotificationMail(String id) {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", id);
        final Element mailElement = transform(baseMail.toXML(), NEW_REQUEST_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(mailElement);
    }
}
