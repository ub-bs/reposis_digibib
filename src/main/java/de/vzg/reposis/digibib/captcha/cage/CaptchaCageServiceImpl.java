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

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.vzg.reposis.digibib.captcha.CaptchaService;

import org.mycore.common.MCRException;
import org.mycore.crypt.MCRCipher;
import org.mycore.crypt.MCRCipherManager;
import org.mycore.crypt.MCRCryptKeyFileNotFoundException;
import org.mycore.crypt.MCRCryptKeyNoPermissionException;

public class CaptchaCageServiceImpl implements CaptchaService {

    private final static String CIPHER_NAME = "captcha";

    private final Set<Token> seenTokens;

    private CaptchaCageServiceImpl() { 
        seenTokens = new HashSet();
    }

    public static CaptchaCageServiceImpl getInstance() {
        return ServiceHolder.INSTANCE;
    }

    @Override
    public boolean validateToken(String encodedToken) throws Exception {
        final Token token = decodeToken(encodedToken);
        final String secret = token.getSecret();
        if (seenTokens.contains(token)) {
            return false;
        }
        seenTokens.add(token);
        return true;
    }

    public static String createEncodedToken(String secret) throws MCRException {
        final Token token = new Token(secret);
        try {
            return encodeToken(token);
        } catch (JsonProcessingException e) {
            throw new MCRException(e);
        }
    }

    private static String encodeToken(Token token) throws JsonProcessingException, MCRException {
        try {
            final String json = new ObjectMapper().writeValueAsString(token);
            final MCRCipher cipher = MCRCipherManager.getCipher(CIPHER_NAME);
            return cipher.encrypt(json);
        } catch (MCRCryptKeyFileNotFoundException | MCRCryptKeyNoPermissionException e) {
            throw new MCRException(e);
        }
    }

    private static Token decodeToken(String encodedToken) throws JsonProcessingException, MCRException {
        try {
            final MCRCipher cipher = MCRCipherManager.getCipher(CIPHER_NAME);
            final String json = cipher.decrypt(encodedToken);
            return new ObjectMapper().readValue(json, Token.class);
        } catch (MCRCryptKeyFileNotFoundException | MCRCryptKeyNoPermissionException e) {
            throw new MCRException(e);
        }
    }

    private static class ServiceHolder {
        static final CaptchaCageServiceImpl INSTANCE = new CaptchaCageServiceImpl();
    }

    private static class Token {

        private String secret;

        private Date timestamp;

        Token() {}

        Token(String secret) {
            this.secret = secret;
            this.timestamp = new Date();
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != this.getClass()) {
                return false;
            }
            final Token other = (Token) obj;
            if ((this.secret == null) ? (other.secret != null) : !this.secret.equals(other.secret)) {
                return false;
            }
            if ((this.timestamp == null) ? (other.timestamp != null) : !this.timestamp.equals(other.timestamp)) {
                return false;
            }
            return true;
        }
    }
}
