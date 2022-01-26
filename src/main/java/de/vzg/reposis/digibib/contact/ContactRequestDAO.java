package de.vzg.reposis.digibib.contact;

import java.util.Collection;

import javax.persistence.EntityManager;

import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;

import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ContactRequestDAO {

    private static ContactRequestDAO instance;

    public static ContactRequestDAO getInstance() {
        if (instance == null) {
            instance = new ContactRequestDAO();
        }
        return instance;
    }

    public Collection<ContactRequest> findAll() {
        final Collection<ContactRequest> contactRequests = MCREntityManagerProvider.getCurrentEntityManager()
                .createNamedQuery("ContactRequest.findAll", ContactRequest.class).getResultList();
        return contactRequests;
    }

    public Collection<ContactRequest> findByObjectID(MCRObjectID objectID) {
        final Collection<ContactRequest> contactRequests = MCREntityManagerProvider.getCurrentEntityManager()
                .createNamedQuery("ContactRequest.findByObjectID", ContactRequest.class)
                .setParameter("objectID", objectID).getResultList();
        return contactRequests;
    }

    public Collection<ContactRequest> findByState(ContactRequestState state) {
        final Collection<ContactRequest> contactRequests = MCREntityManagerProvider.getCurrentEntityManager()
                .createNamedQuery("ContactRequest.findByState", ContactRequest.class)
                .setParameter("state", state).getResultList();
        return contactRequests;
    }

    public ContactRequest findByID(long id) {
        final ContactRequest contactRequest = MCREntityManagerProvider.getCurrentEntityManager()
            .find(ContactRequest.class, id);
        return contactRequest;
    }

    public void save(ContactRequest contactRequest) {
        MCREntityManagerProvider.getCurrentEntityManager().persist(contactRequest);
    }

    public void remove(ContactRequest contactRequest) {
        final EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        entityManager.remove(entityManager.contains(contactRequest) ? contactRequest
                : entityManager.merge(contactRequest));
    }
}
