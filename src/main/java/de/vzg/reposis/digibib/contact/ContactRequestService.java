package de.vzg.reposis.digibib.contact;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import de.vzg.reposis.digibib.contact.exception.InvalidContactRequestException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
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

    private ContactRequestDAO contactRequestDAO;

    private static ContactRequestService instance;

    public static ContactRequestService getInstance() {
        if (instance == null) {
            instance = new ContactRequestService();
        }
        return instance;
    }

    private ContactRequestService() {
        contactRequestDAO = new ContactRequestDAO();
    }

    public ContactRequest getContactRequestByID(long id) {
        return contactRequestDAO.findByID(id);
    }

    public List<ContactRequest> getContactRequests() {
        return List.copyOf(contactRequestDAO.findAll());
    }

    public void saveContactRequest(ContactRequest contactRequest) throws InvalidContactRequestException, MCRException {
        if (!ValidationHelper.validateContactRequest(contactRequest)) {
            throw new InvalidContactRequestException();
        }
        final MCRObjectID objectID = contactRequest.getObjectID();
        if (objectID == null || !MCRMetadataManager.exists(objectID)) {
            throw new MCRException(objectID.toString() + " does not exist.");
        }
        final Date currentDate = new Date();
        contactRequest.setCreated(currentDate);
        contactRequest.setLastModified(currentDate);
        final String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
        contactRequest.setCreatedBy(currentUserID);
        contactRequest.setLastModifiedBy(currentUserID);
        contactRequest.setState(ContactRequestState.ACCEPTED);
        contactRequestDAO.save(contactRequest);
    }

    public void removeContactRequestByID(long id) throws ContactRequestNotFoundException {
        final ContactRequest contactRequest = contactRequestDAO.findByID(id);
        if (contactRequest == null) {
            throw new ContactRequestNotFoundException();
        }
        contactRequestDAO.remove(contactRequest);
    }
}
