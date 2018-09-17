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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aludratest.config.Preferences;
import org.aludratest.config.ValidatingPreferencesWrapper;
import org.aludratest.exception.AutomationException;

/** Configuration object for Selenium 2 services.
 *
 * @author Marcel Malitz
 * @author Joerg Langnickel
 * @author Volker Bergmann
 * @author falbrech */
public final class SeleniumWrapperConfiguration {

    private ValidatingPreferencesWrapper configuration;

    /** Creates a new typed configuration object for the given Preferences object
     *
     * @param configuration Preferences object containing required configuration. */
    public SeleniumWrapperConfiguration(Preferences configuration) {
        this.configuration = new ValidatingPreferencesWrapper(configuration);
    }

    // properties --------------------------------------------------------------

    /**
     * Browser which will be used for testing.
     *
     * @return Browser which will be used for testing.
     */
    public String getBrowser() {
        return configuration.getRequiredStringValue("browser");
    }

    /**
     * URL of the application under test.
     *
     * @return URL of the application under test.
     */
    public String getUrlOfAut() {
        return configuration.getRequiredStringValue("url.of.aut");
    }

    /**
     * Timeout in milliseconds after which a test step stops retrying doing a action.
     *
     * @return Timeout in milliseconds after which a test step stops retrying doing a action.
     */
    public int getTimeout() {
        return configuration.getRequiredIntValue("timeout");
    }

    /**
     * Speed in milliseconds. What means that between each Selenium command Selenium waits x milliseconds where x is the speed.
     *
     * @return Speed in milliseconds.
     */
    public String getSpeed() {
        return configuration.getRequiredStringValue("speed");
    }

    /**
     * Indicates if the application under test shall be closed after test execution.
     *
     * @return <code>true</code> if the application under test shall be closed after test execution.
     */
    public boolean getCloseTestappAfterExecution() {
        return configuration.getRequiredBooleanValue("close.testapp.after.execution");
    }

    /**
     * Returns the browser log level.
     *
     * @return The browser log level.
     */
    public String getBrowserLogLevel() {
        return configuration.getStringValue("browser.log.level", "error");
    }

    /**
     * Indicates if commands shall be highlighted in GUI before performing them.
     *
     * @return <code>true</code> if commands shall be highlighted in GUI before performing them.
     */
    public boolean getHighlightCommands() {
        return configuration.getBooleanValue("highlight.elements", true);
    }

    /**
     * If execution of a action fails the program has to pause until it retries to execute this action again. This value specifies
     * how long the program will pause in milliseconds.
     *
     * @return How long program will pause between retries, in milliseconds.
     */
    public int getPauseBetweenRetries() {
        return configuration.getRequiredIntValue("pause.between.retries");
    }

    public String getScreenshotAttachmentExtension() {
        return configuration.getRequiredStringValue("screenshot.attachment.extension");
    }

    public String getPageSourceAttachmentExtension() {
        return configuration.getRequiredStringValue("page.source.attachment.extension");
    }

    /**
     * Returns the time the framework waits for an activity to start.
     *
     * @return the time the framework waits for an activity to start.
     */
    public int getTaskStartTimeout() {
        return configuration.getIntValue("task.start.timeout", 2000);
    }

    /**
     * Returns the maximum time the framework waits for an activity to finish.
     *
     * @return the maximum time the framework waits for an activity to finish.
     */
    public int getTaskCompletionTimeout() {
        return configuration.getIntValue("task.completion.timeout", 45000);
    }

    /**
     * Returns the polling interval for tasks, in milliseconds.
     *
     * @return The polling interval for tasks, in milliseconds.
     */
    public int getTaskPollingInterval() {
        return configuration.getIntValue("task.polling.interval", 1000);
    }

    /** @return the URL of the application under test as {@link URL} object. */
    public URL getUrlOfAutAsUrl() {
        String urlOfAut = getUrlOfAut();
        try {
            return new URL(urlOfAut);
        }
        catch (MalformedURLException e) {
            throw new AutomationException("Malformed urlOfAut: " + urlOfAut, e);
        }
    }

    /** Returns, for Selenium 2, if a local proxy server shall be used.
     *
     * @return <true> if a local proxy server shall be used, enabling additional HTTP features, <code>false</code> otherwise. */
    public boolean isUsingLocalProxy() {
        return Boolean.valueOf(configuration.getStringValue("use.local.proxy", "true")).booleanValue();
    }

    /** This property is only used for Selenium 2, and only has effect if the local proxy flag is set to <code>true</code>.
     *
     * @return the lowest port number to use for the authenticating proxy as defined in the 'proxy.port.min' setting of the
     *         configuration file, or a default of 19600 if undefined. */
    public int getMinProxyPort() {
        return configuration.getIntValue("proxy.port.min", 19600);
    }

    /** Returns the web driver (browser) name for Selenium 2, and throws a ConfigurationException if it is not set.
     *
     * @return The web driver (browser) name for Selenium 2. */
    public String getDriverName() {
        return configuration.getRequiredStringValue("driver");
    }

    /** Returns the (possibly empty) list of additional arguments to pass to the Browser. Only used for Selenium 2, and only
     * supported by CHROME driver.
     *
     * @return (possibly empty) list of additional arguments to pass to the Browser. */
    public String[] getBrowserArguments() {
        String value = configuration.getStringValue("browser.arguments");
        if (value == null) {
            return new String[0];
        }

        // FIXME do not split across quotation marks. Instead, remove them

        List<String> result = new ArrayList<String>();
        for (String arg : value.split(" ")) {
            arg = arg.trim();
            if (arg.length() > 0) {
                result.add(arg);
            }
        }

        return result.toArray(new String[0]);
    }

    /**
     * Returns, for Selenium 2, if a remote driver shall be used.
     *
     * @return <code>true</code> if a remote driver shall be used, <code>false</code> otherwise.
     */
    public boolean isUsingRemoteDriver() {
        return Boolean.valueOf(configuration.getStringValue("use.remotedriver", "false")).booleanValue();
    }

    /** Returns, for Selenium 2, the TCP timeout to use. If the Selenium Client does not respond within this period of time, the
     * request is aborted, and a SocketTimeoutException will be raised.
     *
     * @return TCP timeout to use, in milliseconds. */
    public int getTcpTimeout() {
        return configuration.getIntValue("tcp.timeout", 5000);
    }

    /** Returns, for Selenium 2, if the safe mode for typing shall be used.
     *
     * @return <code>true</code> if the safe mode for typing shall be used, <code>false</code> otherwise. */
    public boolean isTypeSafemode() {
        return Boolean.valueOf(configuration.getStringValue("type.safemode", "false")).booleanValue();
    }

    /** Returns, for Selenium 2, if z-Index checks shall be performed.
     *
     * @return <code>true</code> if the z-Index checks shall be performed, <code>false</code> otherwise. */
    public boolean isZIndexCheckEnabled() {
        return Boolean.valueOf(configuration.getStringValue("zindex.check.enabled", "true")).booleanValue();
    }

    /** Returns, for Selenium 2, the PhantomJS initialization JavaScript file.
     *
     * @return The PhantomJS initialization JavaScript file, or <code>null</code> if not set. */
    public String getPhantomJsInitScript() {
        return configuration.getStringValue("phantomjs.init.script");
    }

    /** Returns, for Selenium 2, the name of the AJAX framework to auto-check for pending operations, if any.
     *
     * @return The name of the AJAX framework to auto-check for pending operations, or <code>null</code> to not perform any
     *         checks. */
    public String getAutoWaitAjaxFrameworkName() {
        return configuration.getStringValue("auto.wait.for.ajax.framework");
    }

    /** Returns, for Selenium 2, the number of milliseconds to wait after typing into an input component and before tabbing out.
     *
     * @return The number of milliseonds to wait after typing into an input component and before tabbing out. 0 indicates not to
     *         wait. */
    public int getTypeWaitBeforeTab() {
        return configuration.getIntValue("type.wait.before.tab", 0);
    }

    /** Builds a map containing the additional headers which shall be sent to Selenium on Session creation. If nothing is
     * configured, an empty map is returned.
     *
     * @return A map with additional headers for Selenium, never <code>null</code>. */
    public Map<String, String> getAdditionalSeleniumHeaders() {
        String value = configuration.getStringValue("additional.selenium.headers");
        if (value == null) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<String, String>();

        for (String kv : value.split(";")) {
            kv = kv.trim();
            if (!kv.contains("=")) {
                throw new AutomationException("Invalid additional.selenium.headers configuration value");
            }
            String[] kandv = kv.split("=");
            if (kandv.length != 2) {
                throw new AutomationException("Invalid additional.selenium.headers configuration value");
            }

            result.put(kandv[0].trim(), kandv[1].trim());
        }

        return result;
    }
}
