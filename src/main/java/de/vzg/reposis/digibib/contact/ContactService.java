package de.vzg.reposis.digibib.contact;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import de.vzg.reposis.digibib.contact.exception.InvalidMessageException;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.validation.ValidationHelper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRMailer;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.events.MCRShutdownHandler;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.orcid.user.MCRORCIDUser;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.mycore.util.concurrent.MCRTransactionableRunnable;

public class ContactService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static ExecutorService CONTACT_SERVICE = Executors.newFixedThreadPool(3);

    static {
        MCRShutdownHandler.getInstance().addCloseable(new MCRShutdownHandler.Closeable() {

            @Override
            public void prepareClose() {
                // nothing to todo
            }

            @Override
            public int getPriority() {
                return Integer.MIN_VALUE;
            }

            @Override
            public void close() {
                LOGGER.info("Shutting down contact service...");
                CONTACT_SERVICE.shutdown();
            }
        });
    }

    public ContactRequest getContactRequestByID(long id) {
        return ContactRequestDAO.getInstance().findByID(id);
    }

    public void saveContactRequest(ContactRequest contactRequest) {
        if (!ValidationHelper.validateContactRequest(contactRequest)) {
            throw new InvalidMessageException();
        }
        final MCRObjectID objectID = contactRequest.getObjectID();
        if (objectID == null || !MCRMetadataManager.exists(objectID)) {
            throw new MCRException(objectID.toString() + " does not exist.");
        }
        contactRequest.setCreatedBy(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        contactRequest.setState(ContactRequestState.ACCEPTED); // Move to Service
        ContactRequestDAO.getInstance().save(contactRequest);
    }

    public void removeContactRequestByID(long id) throws IllegalArgumentException { // TODO
        final ContactRequest contactRequest = ContactRequestDAO.getInstance().findByID(id);
        if (contactRequest == null) {
            throw new IllegalArgumentException();
        }
        ContactRequestDAO.getInstance().remove(contactRequest);
    }

    public static void contact(ContactRequest contactRequest) throws InvalidMessageException, MCRException {
        CONTACT_SERVICE.execute(new MCRTransactionableRunnable(new ContactTask(contactRequest)));
    }

    static class ContactTask implements Runnable {

        private static final String ATTR_ORCID_ID = "TODO"; // TODO use MCRORCIDUser.ATTR_ORCID_ID from mycore-orcid (private);

        private static final String SENDER_NAME = MCRConfiguration2
                .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.SenderName");

        private static final String MAIL_STYLESHEET = MCRConfiguration2
                .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.Stylesheet");

        private static final String FALLBACK_EMAIL = MCRConfiguration2
                .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackEmail");

        private final ContactRequest contactRequest;

        public ContactTask(ContactRequest contactRequest) {
            this.contactRequest = contactRequest;
        }

        // TODO option to send copy to requester
        // if correspondings should be TO and others BC
        // if no correspondings all should be TO
        @Override
        public void run() {
            LOGGER.info("Started following contact request task: {}", contactRequest.toString());
            final Set<String> recipients = new HashSet();
            final MCRObjectID objectID = contactRequest.getObjectID();
            final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
            final List<Element> authors = getAuthors(object);
            for (Element author : authors) {
                final String orcid = getORCID(author);
                if (orcid != null) {
                    recipients.addAll(getEmails(orcid));
                }
                // TODO extract affiliations
            }
            if (recipients.isEmpty()) {
                recipients.add(FALLBACK_EMAIL);
            }
            final Document mail = createMail(recipients).toXML();
            final Map<String, String> parameters = new HashMap();
            parameters.put("email", contactRequest.getSender());
            parameters.put("id", objectID.toString());
            parameters.put("message", contactRequest.getMessage());
            parameters.put("name", contactRequest.getName());
            final String orcid = contactRequest.getORCID();
            if (orcid != null) {
                parameters.put("orcid", contactRequest.getORCID());
            }
            parameters.put("title", getTitle(object));
            try {
                MCRMailer.sendMail(mail, MAIL_STYLESHEET, parameters);
            } catch (Exception e) {
                LOGGER.error(e);
                throw new MCRException("Cannot send email.");
            }
            /* if (contactRequest.getSendCopy()) {
                // TODO send mail to sender
            } */
        }

        private static String getORCID(Element element) {
            return element.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE).stream()
                    .filter(e -> "orcid".equals(e.getAttributeValue("type"))).map(Element::getText).findFirst()
                    .orElse(null);
        }

        private static Set<String> getEmails(String orcid) {
            final Set<String> emails = new HashSet();
            final List<MCRUser> users = getUsersByORCID(orcid);
            for (MCRUser user : users) {
                final MCRORCIDUser orcidUser = new MCRORCIDUser(user);
                try {
                    // emails.addAll(orcidUser.getProfile().getEmails()); TODO
                    LOGGER.info(orcidUser.getORCID());
                } catch (Exception e) {
                    LOGGER.warn("Skipping {}, cannot fetch email", user.getUserName());
                }
            }
            return emails;
        }

        private static String getTitle(MCRObject object) { // TODO move wrapper to member
            final MCRMODSWrapper wrapper = new MCRMODSWrapper(object);
            return wrapper.getElementValue("mods:titleInfo[1]/mods:title[1]");
        }

        private static List<Element> getAuthors(MCRObject object) {
            final MCRMODSWrapper wrapper = new MCRMODSWrapper(object);
            return wrapper.getElements("mods:name[@type='personal']");
        }

        private static List<MCRUser> getUsersByORCID(String orcid) {
            return MCRUserManager.getUsers(ATTR_ORCID_ID, orcid).collect(Collectors.toList());
        }

        private static EMail createMail(Set<String> recipients) {
            final EMail mail = new EMail();
            mail.to = List.copyOf(recipients);
            mail.from = SENDER_NAME;
            return mail;
        }
    }
}
