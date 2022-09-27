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

package de.vzg.reposis.digibib.contact.rsc;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.ext.Provider;

import de.vzg.reposis.digibib.captcha.cage.CaptchaCageServiceImpl;

@Provider
@ContactCheckCageCaptcha
public class ContactCageCaptchaFilter implements javax.ws.rs.container.ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final String token = requestContext.getHeaderString("X-Captcha");
        if (token == null || !CaptchaCageServiceImpl.getInstance().validateToken(token)) {
            throw new BadRequestException();
        }
    }
}
