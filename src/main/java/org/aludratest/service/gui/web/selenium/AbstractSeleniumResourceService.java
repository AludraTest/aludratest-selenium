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
package org.aludratest.service.gui.web.selenium;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aludratest.config.ConfigProperty;
import org.aludratest.config.Configurable;
import org.aludratest.config.MutablePreferences;
import org.aludratest.config.Preferences;
import org.aludratest.exception.AutomationException;
import org.databene.commons.StringUtil;

/** Abstract base class for both Selenium Resource Service implementations, providing common properties and methods.
 *
 * @author falbrech */
@ConfigProperty(name = AbstractSeleniumResourceService.SELENIUM_URLS_PROP, type = String.class, description = "Comma-separated list of Selenium URLs to use. The URLs will be used in a round-robin manner. It is possible that a single Selenium URL is used by multiple threads (if multiple threads are configured in aludratest.properties)", defaultValue = "http://localhost:4444/wd/hub")
public abstract class AbstractSeleniumResourceService implements SeleniumResourceService, Configurable {

    /** Property name for selenium.properties. */
    public static final String SELENIUM_URLS_PROP = "selenium.urls";

    @Override
    public String getPropertiesBaseName() {
        return "selenium";
    }

    @Override
    public void fillDefaults(MutablePreferences preferences) {
    }

    @Override
    public void configure(Preferences preferences) throws AutomationException {
        String seleniumUrlsValue = preferences.getStringValue(SELENIUM_URLS_PROP);

        List<URL> seleniumUrls;

        if (StringUtil.isEmpty(seleniumUrlsValue)) {
            seleniumUrls = Collections.emptyList();
        }
        else {
            seleniumUrls = new ArrayList<URL>();
            for (String s : seleniumUrlsValue.split(",")) {
                s = s.trim();
                try {
                    seleniumUrls.add(new URL(s));
                }
                catch (MalformedURLException e) {
                    throw new AutomationException("Invalid Selenium URL configured", e);
                }
            }
        }

        urlsConfigured(seleniumUrls);
    }

    protected abstract void urlsConfigured(List<URL> seleniumUrls);

}
