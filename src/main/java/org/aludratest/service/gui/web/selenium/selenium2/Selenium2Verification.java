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
package org.aludratest.service.gui.web.selenium.selenium2;

import org.aludratest.exception.AutomationException;
import org.aludratest.exception.FunctionalFailure;
import org.aludratest.service.gui.web.WebGUIVerification;
import org.aludratest.service.locator.element.GUIElementLocator;
import org.databene.commons.Validator;

/**
 * This class is providing all operations for verify elements on a WebGUI with Selenium2
 * @author Marcel Malitz
 * @author Joerg Langnickel
 * @author Volker Bergmann
 */
public class Selenium2Verification extends AbstractSelenium2Action implements WebGUIVerification {

    Selenium2Verification(Selenium2Wrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void assertTextMatches(String elementType, String operation, GUIElementLocator locator, Validator<String> validator) {
        wrapper.waitForTextValidity(locator, validator);
    }

    @Override
    public void assertVisible(String elementType, String operation, GUIElementLocator locator) {
        try {
            wrapper.waitUntilVisible(locator, getTimeout());
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertNotVisible(String elementType, String elementName, GUIElementLocator locator) {
        try {
            wrapper.waitUntilNotVisible(locator, getTimeout());
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertEditable(String elementType, String operation, GUIElementLocator locator) {
        try {
            wrapper.waitUntilEditable(locator, getTimeout());
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertNotEditable(String elementType, String operation, GUIElementLocator locator) {
        try {
            wrapper.waitUntilNotEditable(locator, getTimeout());
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertEnabled(String elementType, String elementName, GUIElementLocator locator) {
        try {
            wrapper.waitUntilEnabled(locator, getTimeout());
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertNotEnabled(String elementType, String elementName, GUIElementLocator locator) {
        try {
            wrapper.waitUntilNotEnabled(locator, getTimeout());
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertElementPresent(String elementType, String operation, GUIElementLocator locator) {
        try {
            long timeout = getConfiguration().getTimeout();
            wrapper.waitUntilPresent(locator, timeout);
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertChecked(String elementType, String operation, GUIElementLocator locator) {
        assertChecked(elementType, operation, true, locator);
    }

    @Override
    public void assertChecked(String elementType, String operation, boolean expected, GUIElementLocator locator) {
        if (wrapper.isChecked(locator) != expected) {
            String message = "Checkbox or Radiobutton is " + (expected ? "unchecked" : "checked");
            throw new FunctionalFailure(message);
        }
    }

    @Override
    public void assertHasFocus(String elementType, String operation, GUIElementLocator locator) {
        final boolean hasFocus = wrapper.hasFocus(locator);
        if (!hasFocus) {
            throw new FunctionalFailure("The element does not have the focus.");
        }
    }

    @Override
    public void assertHasValues(String elementType, String operation, GUIElementLocator locator, String[] expectedValues,
            boolean checkOrder) {
        try {
            wrapper.waitForDropDownValues(locator, expectedValues, false, checkOrder);
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertHasLabels(String elementType, String operation, GUIElementLocator locator, String[] expectedLabels,
            boolean checkOrder) {
        checkLabels(expectedLabels, locator, false, checkOrder);
    }

    @Override
    public void assertValueMatches(String elementType, String operation, final GUIElementLocator locator,
            Validator<String> validator) {
        wrapper.waitForValueValidity(locator, validator);
    }

    @Override
    public void assertContainsLabels(String elementType, String operation, GUIElementLocator locator, String[] labels) {
        checkLabels(labels, locator, true, false);
    }

    @Override
    public void assertContainsValues(String elementType, String elementName, GUIElementLocator locator, String[] values) {
        try {
            wrapper.waitForDropDownValues(locator, values, true, false);
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertElementNotPresent(String elementType, String operation, GUIElementLocator locator) {
        try {
            wrapper.waitUntilElementNotPresent(locator, getTimeout());
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

    @Override
    public void assertDropDownEntrySelectionMatches(String elementType, String operation, final GUIElementLocator locator,
            Validator<String> validator) {
        wrapper.waitForSelectedLabelValidity(locator, validator);
    }

    private void checkLabels(String[] labels, GUIElementLocator dropDownLocator, boolean contains, boolean checkOrder) {
        try {
            wrapper.waitForDropDownEntries(dropDownLocator, labels, contains, checkOrder);
        }
        catch (AutomationException e) { // NOSONAR
            throw new FunctionalFailure(e.getMessage());
        }
    }

}
