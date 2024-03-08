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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

import com.github.cage.Cage;
import com.github.cage.GCage;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.vzg.reposis.digibib.captcha.cage.CaptchaCageServiceImpl;

import org.mycore.common.MCRException;

@Singleton
@Path("/captchaCage")
public class CaptchaCageResource {

    private final Cache<String, Boolean> generatedSecrets;

    private final Cage cage;

    public CaptchaCageResource() {
        generatedSecrets = CacheBuilder.newBuilder().maximumSize(32768).expireAfterWrite(60, TimeUnit.MINUTES).build();
        cage = new GCage();
    }

    @GET
    @Produces("image/" + Cage.DEFAULT_FORMAT)
    public StreamingOutput getCaptcha(@Context HttpServletResponse resp) {
        final String secret = cage.getTokenGenerator().next();
        generatedSecrets.put(secret, Boolean.TRUE);
        setResponseHeaders(resp);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                cage.draw(secret, os);
            }
        };
    }

    @POST
    @Path("/userverify")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public TokenResponse verifySecret(String secret) throws MCRException {
        if (generatedSecrets.getIfPresent(secret) == null) {
            throw new BadRequestException();
        }
        generatedSecrets.invalidate(secret);
        final String token = CaptchaCageServiceImpl.createEncodedToken(secret);
        return new TokenResponse(token);
    }

    private void setResponseHeaders(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-cache, no-store");
        resp.setHeader("Pragma", "no-cache");
        long time = System.currentTimeMillis();
        resp.setDateHeader("Last-Modified", time);
        resp.setDateHeader("Date", time);
        resp.setDateHeader("Expires", time);
    }

    static class TokenResponse {

        private boolean verified;

        private String verifiedToken;

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
