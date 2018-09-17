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

import java.net.URL;

import org.aludratest.config.InternalComponent;
import org.aludratest.service.gui.web.selenium.SeleniumWrapperConfiguration;
import org.openqa.selenium.WebDriver;

/** Interface for components being able to create Selenium WebDriver objects based on module configuration. aludratest-selenium
 * ships a default implementation for this interface. Plug-Ins may provide an own implementation which e.g. wraps the WebDriver to
 * support other external tools (e.g. NeoLoad).
 *
 * @author falbrech */
@InternalComponent(singleton = true)
public interface SeleniumWebDriverFactory {

    /** Creates a new WebDriver for Selenium working directly from the current JVM, on this machine.
     *
     * @param configuration Configuration to use to create the WebDriver object.
     *
     * @return A new WebDriver object to work with. */
    public WebDriver createLocalWebDriver(SeleniumWrapperConfiguration configuration);

    /** Creates a new WebDriver for controlling a Selenium (or WebDriver protocol compatible server) on a remote machine.
     *
     * @param seleniumUrl URL of the Selenium (or WebDriver server) to use.
     * @param configuration Configuration to use to create the WebDriver object.
     *
     * @return A new WebDriver object to work with. */
    public WebDriver createRemoteWebDriver(URL seleniumUrl, SeleniumWrapperConfiguration configuration);

}
