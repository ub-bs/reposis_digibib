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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSL2XMLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.xml.sax.SAXException;

public class ContactSendTask implements Callable<Void> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SENDER_NAME = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.SenderName");

    private static final String MAIL_STYLESHEET = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.Stylesheet");

    private final ContactRequest contactRequest;

    public ContactSendTask(ContactRequest contactRequest) {
        this.contactRequest = contactRequest;
    }

    @Override
    public Void call() throws Exception {
        LOGGER.info("Sending contact request: ", contactRequest.getId());
        final EMail baseMail = createBaseMail();
        final Map<String, String> properties = new HashMap();
        properties.put("email", contactRequest.getSender());
        properties.put("id", contactRequest.getObjectID().toString());
        properties.put("message", contactRequest.getMessage());
        properties.put("name", contactRequest.getName());
        properties.put("title", contactRequest.getObjectID().toString());
        final String orcid = contactRequest.getORCID();
        if (orcid != null) {
            properties.put("orcid", orcid);
        }
        final Element mailElement = transform(baseMail.toXML(), MAIL_STYLESHEET, properties).getRootElement();
        final EMail mail = EMail.parseXML(mailElement);
        ContactMailService.getInstance().sendMail(mail);
        if (contactRequest.isSendCopy()) {
            // TODO send copy
        }
        return null;
    }

    private EMail createBaseMail() {
        final EMail mail = new EMail();
        final Set<String> recipients = contactRequest.getRecipients().stream().map(ContactRecipient::getEmail)
                .collect(Collectors.toSet());
        mail.to = List.copyOf(recipients);
        mail.from = SENDER_NAME;
        return mail;
    }

    private static Document transform(Document input, String stylesheet, Map<String, String> parameters)
        throws IOException, JDOMException, SAXException {
        MCRJDOMContent source = new MCRJDOMContent(input);
        MCRXSL2XMLTransformer transformer = MCRXSL2XMLTransformer.getInstance("xsl/" + stylesheet + ".xsl");
        MCRParameterCollector parameterCollector = MCRParameterCollector.getInstanceFromUserSession();
        parameterCollector.setParameters(parameters);
        return transformer.transform(source, parameterCollector).asXML();
    }
}
