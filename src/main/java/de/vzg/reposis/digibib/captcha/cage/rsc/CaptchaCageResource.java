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

package de.vzg.reposis.digibib.captcha.cage.rsc;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.github.cage.Cage;
import com.github.cage.GCage;

import de.vzg.reposis.digibib.captcha.cage.CaptchaCageServiceImpl;

import org.mycore.common.MCRException;

@Path("/captchaCage")
public class CaptchaCageResource {

    private static final Cage cage = new GCage();

    private static final Set<String> generatedSecrets = new HashSet();

    private static final Set<String> seenSecrets = new HashSet();

    @GET
    @Produces("image/" + Cage.DEFAULT_FORMAT)
    public StreamingOutput getCaptcha(@Context HttpServletResponse resp) {
        final String secret = cage.getTokenGenerator().next();
        generatedSecrets.add(secret);
        setResponseHeaders(resp);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                cage.draw(secret, os);
            }
        };
    };

    @POST
    @Path("/verify")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public TokenResponse verifySecret(String secret) throws MCRException {
        if (!generatedSecrets.contains(secret) || seenSecrets.contains(secret)) {
            throw new BadRequestException();
        }
        seenSecrets.add(secret);
        final String token = CaptchaCageServiceImpl.createEncodedToken(secret);
        return new TokenResponse(token);
    };

    private void setResponseHeaders(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-cache, no-store");
        resp.setHeader("Pragma", "no-cache");
        long time = System.currentTimeMillis();
        resp.setDateHeader("Last-Modified", time);
        resp.setDateHeader("Date", time);
        resp.setDateHeader("Expires", time);
    }

    private static class TokenResponse {

        private boolean verified;

        private String verifiedToken;

        TokenResponse() { }

        TokenResponse(String verifiedToken) {
            this.verified = true;
            this.verifiedToken = verifiedToken;
        }

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        public String getVerifiedToken() {
            return verifiedToken;
        }
        
        public void setVerifiedToken(String verifiedToken) {
            this.verifiedToken = verifiedToken;
        }
    }
}
