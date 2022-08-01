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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCRShutdownHandler;
import org.mycore.common.events.MCRStartupHandler;

import javax.servlet.ServletContext;

public class ContactTaskManager implements MCRStartupHandler.AutoExecutable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static void addShutdownHandler(ExecutorService executorService) {
        MCRShutdownHandler.getInstance().addCloseable(() -> {
            executorService.shutdown();
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Interupted wait for termination.", e);
            }
        });
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE + 1000;
    }

    @Override
    public void startUp(ServletContext servletContext) {
        LOGGER.info("Started ContactTaskManager");
        if (servletContext == null) {
            return; // do not run in CLI
        }
        ScheduledExecutorService cronExecutorService = Executors.newSingleThreadScheduledExecutor();
        addShutdownHandler(cronExecutorService);
        cronExecutorService.scheduleWithFixedDelay(new ContactCollectorTask(), 1, 1, TimeUnit.MINUTES);
    }
}
