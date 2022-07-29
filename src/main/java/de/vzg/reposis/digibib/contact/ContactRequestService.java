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

import java.util.Date;
import java.util.List;

import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.exception.InvalidContactRequestException;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.validation.ValidationHelper;

import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ContactRequestService {

    private ContactRequestDAO contactRequestDAO;

    private static ContactRequestService instance;

    public static ContactRequestService getInstance() {
        if (instance == null) {
            instance = new ContactRequestService();
        }
        return instance;
    }

    private ContactRequestService() {
        contactRequestDAO = new ContactRequestDAOImpl();
    }

    public ContactRequest getContactRequestByID(long id) {
        return contactRequestDAO.findByID(id);
    }

    public List<ContactRequest> getContactRequests() {
        return List.copyOf(contactRequestDAO.findAll());
    }

    public void insertContactRequest(ContactRequest contactRequest) throws InvalidContactRequestException, MCRException {
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
        contactRequest.setState(ContactRequestState.RECEIVED);
        contactRequestDAO.insert(contactRequest);
    }

    public void removeContactRequestByID(long id) throws ContactRequestNotFoundException {
        final ContactRequest contactRequest = contactRequestDAO.findByID(id);
        if (contactRequest == null) {
            throw new ContactRequestNotFoundException();
        }
        contactRequestDAO.remove(contactRequest);
    }

    public void rejectContactRequest(long id) {
        final ContactRequest contactRequest = contactRequestDAO.findByID(id);
        if (contactRequest == null) {
            throw new ContactRequestNotFoundException();
        }
        if (!ContactRequestState.READY.equals(contactRequest.getState())) {
            throw new ContactException("Contact request state is not ready.");
        }
        updateState(contactRequest, ContactRequestState.REJECTED);
    }

    public void forwardContactRequest(long id) {
        final ContactRequest contactRequest = contactRequestDAO.findByID(id);
        if (contactRequest == null) {
            throw new ContactRequestNotFoundException();
        }
        if (!ContactRequestState.READY.equals(contactRequest.getState())) {
            throw new ContactException("Contact request state is not ready.");
        }
        updateState(contactRequest, ContactRequestState.ACCEPTED);
    }

    private void updateState(ContactRequest request, ContactRequestState state) {
        request.setState(state);
        contactRequestDAO.update(request);
    }
}
