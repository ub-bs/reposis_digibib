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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.processing.MCRProcessableStatus;
import org.mycore.mcr.cronjob.MCRCronjob;
import org.mycore.util.concurrent.MCRFixedUserCallable;

/**
 * This class implements a cronjob that processes bounces mails.
 */
public class ContactBouncedForwardingsCronjob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getDescription() {
        return "Processes bounced messages.";
    }

    @Override
    public void runJob() {
        getProcessable().setStatus(MCRProcessableStatus.processing);
        getProcessable().setProgress(0);
        try {
            new MCRFixedUserCallable<>(() -> {
                ContactServiceImpl.getInstance().handleBouncedMessages();
                return null;
            }, MCRSystemUserInformation.getJanitorInstance()).call();
        } catch (Exception e) {
            LOGGER.error("Error while handling bounces messages", e);
        }
        getProcessable().setProgress(100);
    }
}
