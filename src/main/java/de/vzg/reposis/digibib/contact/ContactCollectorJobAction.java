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

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;
import org.mycore.util.concurrent.MCRTransactionableCallable;

import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;

/**
 * Implements a job action that collects mails for contact requests.
 */
public class ContactCollectorJobAction extends MCRJobAction {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String REQUEST_ID = "request_id";

    /**
     * Constructs new {@link ContactCollectorJobAction}.
     *
     * @param job job
     */
    public ContactCollectorJobAction() {

    }

    /**
     * Constructs new {@link ContactCollectorJobAction} from {@link MCRJob}.
     *
     * @param job job
     */
    public ContactCollectorJobAction(MCRJob job) {
        super(job);
    }

    /**
     * Constructs and returns new {@link ContactCollectorJobAction}.
     *
     * @param requestId request id
     * @return job
     */
    public static MCRJob createJob(UUID requestId) {
        final MCRJob job = new MCRJob(ContactCollectorJobAction.class);
        job.setParameter(REQUEST_ID, requestId.toString());
        return job;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public String name() {
        return getClass().getName();
    }

    @Override
    public void execute() throws ExecutionException {
        final UUID requestId = getRequestId();
        LOGGER.info("Started to collect contact persons for request: {}", requestId);
        MCRSessionMgr.unlock();
        final MCRSession session = MCRSessionMgr.getCurrentSession();
        MCRUserInformation savedUserInformation = session.getUserInformation();
        session.setUserInformation(MCRSystemUserInformation.getGuestInstance());
        session.setUserInformation(MCRSystemUserInformation.getJanitorInstance());
        final ContactService service = ContactServiceImpl.getInstance();
        try {
            new MCRTransactionableCallable<>(() -> {
                service.collectContacts(requestId);
                return null;
            }).call();
        } catch (ContactRequestNotFoundException e) {
            // ignore
        } catch (Exception e) {
            LOGGER.error("Job action failed: ", e);
            throw new ExecutionException(e);
        } finally {
            session.setUserInformation(MCRSystemUserInformation.getGuestInstance());
            session.setUserInformation(savedUserInformation);
        }
    }

    private UUID getRequestId() {
        return UUID.fromString(job.getParameter(REQUEST_ID));
    }

    @Override
    public void rollback() {
        // nothing to rollback
    }

}
