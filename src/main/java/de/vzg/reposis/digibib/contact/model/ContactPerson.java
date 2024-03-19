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

package de.vzg.reposis.digibib.contact.model;

/**
 * Define contact person model.
 */
public class ContactPerson {

    private String name;

    private String mail;

    private String origin;

    private boolean enabled;

    private ContactForwarding forwarding;

    /**
     * Constructs new recipient with recipient.
     */
    public ContactPerson(String name, String mail, String origin) {
        setName(name);
        setMail(mail);
        setOrigin(origin);
    }

    /**
     * Returns name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns mail address.
     *
     * @return mail address
     */
    public String getMail() {
        return mail;
    }

    /**
     * Sets mail address.
     *
     * @param mail mail address
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Returns origin.
     *
     * @return
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets origin.
     *
     * @param origin origin
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * Returns if recipient is enabled.
     *
     * @return true if recipient is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets if recipient is enabled.
     *
     * @param enabled true if recipient is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns forwarding.
     *
     * @return forwarding
     */
    public ContactForwarding getForwarding() {
        return forwarding;
    }

    /**
     * Sets forwarding.
     *
     * @param forwarding forwarding
     */
    public void setForwarding(ContactForwarding forwarding) {
        this.forwarding = forwarding;
    }
}
