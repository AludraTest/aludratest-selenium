/*
 * Copyright (C) 2010-2014 Hamburg Sud and the contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aludratest.service.gui.web.selenium.util;

import java.io.StringReader;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.aludratest.exception.AutomationException;
import org.aludratest.service.locator.element.XPathLocator;
import org.aludratest.util.MostRecentUseCache;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

/** Utility class containing a static MRU cache for parsed HTML documents to enable faster XPath evaluations.
 * 
 * @author falbrech */
public final class DocCache {

    private DocCache() {
    }

    private static final MostRecentUseCache.Factory<String, Document> docFactory = new MostRecentUseCache.Factory<String, Document>() {
        @Override
        public Document create(String html) {
            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setShowWarnings(false);
            tidy.setShowErrors(0);
            tidy.setXmlOut(false);
            Document document = tidy.parseDOM(new StringReader(html), null);
            return document;
        }
    };

    /* Use an MRU cache for HTML -> DOM mappings. If executing several XPaths on same HTML, this increases performance
     * significantly. */
    private static final MostRecentUseCache<String, Document> docCache = new MostRecentUseCache<String, Document>(docFactory, 50);

    public static NodeList evalXPathInHTML(XPathLocator locator, String html) {
        return evalXPathInHTML(locator.toString(), html);
    }

    public static NodeList evalXPathInHTML(String xpath, String html) {
        try {
            Document document = docCache.get(html);
            XPathExpression expression = XPathFactory.newInstance().newXPath().compile(xpath);
            return (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        }
        catch (XPathExpressionException e) {
            throw new AutomationException("Illegal XPath: " + xpath, e);
        }
    }

    public static String evalXPathInHTMLAsString(String xpath, String html) {
        try {
            Document document = docCache.get(html);
            XPathExpression expression = XPathFactory.newInstance().newXPath().compile(xpath);
            return (String) expression.evaluate(document, XPathConstants.STRING);
        }
        catch (XPathExpressionException e) {
            throw new AutomationException("Illegal XPath: " + xpath, e);
        }
    }

}
