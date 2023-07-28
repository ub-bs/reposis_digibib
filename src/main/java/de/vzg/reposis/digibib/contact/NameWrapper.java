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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.orcid2.util.MCRIdentifier;

public class NameWrapper {

    private static final String FAMILY_NAME_TYPE = "familyName";

    private static final String GIVEN_NAME_TYPE = "givenName";

    private Element element;

    public NameWrapper(Element element) {
        this.element = element;
    }

    /**
     * Extracts and returns the name.
     * 
     * @return the name or null
     */
    public String getName() {
        String name = element.getChildTextTrim("mods:displayForm", MCRConstants.MODS_NAMESPACE);
        if (name == null) {
            final List<Element> nameParts = element.getChildren("mods:namePart", MCRConstants.MODS_NAMESPACE);
            if (!nameParts.isEmpty()) {
                if (nameParts.size() == 1 && nameParts.get(0).getAttribute("type") == null) {
                    return nameParts.get(0).getTextTrim();
                }
                final List<String> givenNames = new ArrayList();
                String familyName = null;
                for (Element namePart : nameParts) {
                    switch(namePart.getAttributeValue("type")) {
                        case FAMILY_NAME_TYPE -> familyName = namePart.getTextTrim();
                        case GIVEN_NAME_TYPE -> givenNames.add(namePart.getText());
                        default -> {}
                    };
                }
                if (familyName != null) {
                    name = String.format(Locale.ROOT, "%s, ", familyName);
                }
                name += givenNames.stream().collect(Collectors.joining(" "));
            } else {
                return null;
            }
        }
        return name;
    }

    /**
     * Returns ORCID iD.
     * 
     * @return ORCID iD or null
     */
    public String getORCID() {
        return element.getChildren("mods:nameIdentifier", MCRConstants.MODS_NAMESPACE).stream()
            .filter(e -> Objects.equals(e.getAttributeValue("type"), "orcid"))
            .map(Element::getTextTrim).findFirst().orElse(null);
    }

    /**
     * Returns if ORCID iD exists.
     *
     * @return true if ORCID iD exists
     */
    public boolean hasORCID() {
        return element.getChildren("mods:nameIdentifier", MCRConstants.MODS_NAMESPACE).stream()
            .anyMatch(e -> Objects.equals(e.getAttributeValue("type"), "orcid"));
    }

    /**
     * Returns a List of affiliation names.
     * 
     * @return List of affiliation names
     */
    public List<String> getAffiliationNames() {
        return element.getChildren("mods:affiliation", MCRConstants.MODS_NAMESPACE).stream()
            .map(Element::getTextTrim).toList();
    }

    /**
     * Return the Element.
     * 
     * @return the Element
     */
    public Element getElement() {
        return element;
    }
}
