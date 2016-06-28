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
package org.aludratest.service.gui.web.selenium.selenium2.condition;

import org.aludratest.service.gui.web.selenium.selenium2.LocatorSupport;
import org.aludratest.service.locator.element.GUIElementLocator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

/** Parent class for {@link ExpectedCondition} implementations that assume that an element is <b>not</b> existing (present) at all
 * in a page, or, if the element is present, assume other conditions (in the subclasses). <br>
 * An example is the {@link ElementNotVisibleCondition} class, which is fulfilled as soon as an element is not present at all, or
 * the element is not visible.
 * 
 * @author falbrech */
public abstract class NegativeWebElementCondition extends AbstractElementCondition<Boolean> {

    /** Full constructor.
     * @param locator
     * @param locatorSupport */
    public NegativeWebElementCondition(GUIElementLocator locator, LocatorSupport locatorSupport) {
        super(locator, locatorSupport);
    }

    @Override
    public final Boolean apply(WebDriver driver) {
        this.message = null;
        WebElement element = findElementImmediately();
        // not present would perfectly be okay
        if (element == null) {
            return Boolean.TRUE;
        }
        return applyOnElement(element);
    }

    protected abstract Boolean applyOnElement(WebElement element);

}
