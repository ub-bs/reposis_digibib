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

import java.io.IOException;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSL2XMLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.xml.sax.SAXException;

/**
 * This class implements common ulitity methods.
 */
public class ContactUtils {

    /**
     * This method transforms a given document with a given stylesheet name and
     * parameters.
     * 
     * @param input      the document
     * @param stylesheet the stylesheet name
     * @param parameters a map of parameters
     * @return the transformed document
     * @throws IOException   if stylesheet cannot be loaded
     * @throws JDOMException if document is faulty
     * @throws SAXException  if stylesheet is faulty or it cannot be applied
     */
    public static Document transform(Document input, String stylesheet, Map<String, String> parameters)
        throws IOException, JDOMException, SAXException {
        MCRJDOMContent source = new MCRJDOMContent(input);
        MCRXSL2XMLTransformer transformer = MCRXSL2XMLTransformer.getInstance("xsl/" + stylesheet + ".xsl");
        MCRParameterCollector parameterCollector = MCRParameterCollector.getInstanceFromUserSession();
        parameterCollector.setParameters(parameters);
        return transformer.transform(source, parameterCollector).asXML();
    }
}
