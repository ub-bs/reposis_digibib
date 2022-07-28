package de.vzg.reposis.digibib.contact;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

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

public class ContactRequestService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Inject
    private ContactRequestDAO contactRequestDAO;

    private static ContactRequestService instance;

    public static ContactRequestService getInstance() {
        if (instance == null) {
            instance = new ContactRequestService();
        }
        return instance;
    }

    public ContactRequestService() { }

    public ContactRequest getContactRequestByID(long id) {
        return contactRequestDAO.findByID(id);
    }

    public List<ContactRequest> getContactRequests() {
        return List.copyOf(contactRequestDAO.findAll());
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
        LOGGER.info(contactRequest.toString());
        contactRequestDAO.save(contactRequest);
    }

    public void removeContactRequestByID(long id) throws IllegalArgumentException { // TODO
        final ContactRequest contactRequest = contactRequestDAO.findByID(id);
        if (contactRequest == null) {
            throw new IllegalArgumentException();
        }
        contactRequestDAO.remove(contactRequest);
    }
}
