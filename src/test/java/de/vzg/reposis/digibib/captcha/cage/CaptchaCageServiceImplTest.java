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

package de.vzg.reposis.digibib.captcha.cage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.util.Date;

import de.vzg.reposis.digibib.captcha.cage.CaptchaCageServiceImpl.Token;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRClassTools;
import org.mycore.common.MCRTestCase;
import org.mycore.common.config.MCRConfigurationDir;

public class CaptchaCageServiceImplTest extends MCRTestCase {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String KEYFILE_NAME = "captcha.key";

    private void copyKeyFile() throws IOException {
        final InputStream input = MCRClassTools.getClassLoader().getResourceAsStream(KEYFILE_NAME);
        final File keyFile = MCRConfigurationDir.getConfigFile("data/" + KEYFILE_NAME);
        FileUtils.copyInputStreamToFile(input, keyFile);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        copyKeyFile();
    }

    @Test
    public void testValidateToken() throws IOException {
        final CaptchaCageServiceImpl service = CaptchaCageServiceImpl.getInstance();
        LOGGER.info("Checking invalid token");
        assertFalse(service.validateToken("trash"));
        final String encodedToken = CaptchaCageServiceImpl.createEncodedToken("test");
        LOGGER.info("Checking valid token");
        assertTrue(service.validateToken(encodedToken));
        LOGGER.info("Checking valid, but already used token");
        assertFalse(service.validateToken(encodedToken));
        LOGGER.info("Checking valid, but expired token");
        final Token expiredToken = new Token("test");
        expiredToken.setTimestamp(new Date(System.currentTimeMillis() - 6 * 60 * 1000));
        assertFalse(service.validateTokenExpiration(expiredToken));
    }
}
