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

import org.aludratest.config.ConfigProperties;
import org.aludratest.config.ConfigProperty;
import org.aludratest.config.Preferences;
import org.aludratest.service.AbstractConfigurableAludraService;
import org.aludratest.service.ComponentId;
import org.aludratest.service.Implementation;
import org.aludratest.service.gui.component.GUIComponentFactory;
import org.aludratest.service.gui.component.impl.DefaultGUIComponentFactory;
import org.aludratest.service.gui.web.AludraWebGUI;
import org.aludratest.service.gui.web.WebGUICondition;
import org.aludratest.service.gui.web.WebGUIInteraction;
import org.aludratest.service.gui.web.WebGUIVerification;
import org.aludratest.service.gui.web.selenium.SeleniumResourceService;
import org.aludratest.service.gui.web.selenium.SeleniumWrapperConfiguration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.LoggerFactory;

/** Implements the interface {@link AludraWebGUI} using Selenium 2 functionality to access the web GUI.
 * @author Volker Bergmann */
@Implementation({ AludraWebGUI.class })
@ConfigProperties({
        @ConfigProperty(name = "timeout", type = int.class, description = "Timeout in milliseconds after which a test step stops retrying doing a action.", defaultValue = "15000"),
        @ConfigProperty(name = "speed", type = int.class, description = "Speed in milliseconds. What means that between each Selenium command Selenium waits x milliseconds where x is the speed.", defaultValue = "50"),
        @ConfigProperty(name = "browser.log.level", type = String.class, description = "The browser log level. One of debug, info, warn, error.", defaultValue = "error"),
        @ConfigProperty(name = "highlight.elements", type = boolean.class, description = "Activates or deactivates highlighting of web GUI elements currently being used.", defaultValue = "true"),
        @ConfigProperty(name = "pause.between.retries", type = int.class, description = "If execution of an action fails, Selenium has to pause until it retries to execute this action again. This value specifies how long the program will pause, in milliseconds.", defaultValue = "100"),
        @ConfigProperty(name = "screenshot.attachment.extension", type = String.class, description = "The file extension to use for screenshot attachments.", defaultValue = "png"),
        @ConfigProperty(name = "page.source.attachment.extension", type = String.class, description = "The file extension to use for HTML page source attachments.", defaultValue = "html"),
        @ConfigProperty(name = "task.start.timeout", type = int.class, description = "The time the Selenium service waits for an activity to start, in milliseconds.", defaultValue = "2000"),
        @ConfigProperty(name = "task.completion.timeout", type = int.class, description = "The time the Selenium service waits for an activity to finish, in milliseconds.", defaultValue = "45000"),
        @ConfigProperty(name = "task.polling.interval", type = int.class, description = "The polling interval for checking task states, in milliseconds.", defaultValue = "1000"),
        @ConfigProperty(name = "use.local.proxy", type = boolean.class, description = "If true, a local HTTP proxy will be used to allow adding custom HTTP headers. If set to false, no proxy will be used, but the method addCustomHttpHeaderCommand will have no effect.", defaultValue = "true"),
        @ConfigProperty(name = "proxy.port.min", type = int.class, description = "The lowest port number to use for the authenticating proxy.", defaultValue = "19600"),
        @ConfigProperty(name = "driver", type = String.class, description = "The Selenium 2 driver name. Have a look at the org.aludratest.service.gui.web.selenium.selenium2.Drivers enumeration for potential values", defaultValue = "FIREFOX"),
        @ConfigProperty(name = "use.remotedriver", type = boolean.class, description = "If true, use Selenium Remote Driver (talk to Selenium RC), otherwise, directly use driver class.", defaultValue = "false"),
        @ConfigProperty(name = "browser.arguments", type = String.class, description = "Space-separated list of arguments to pass to the browser. Currently, only the CHROME driver supports additional arguments.", required = false),
        @ConfigProperty(name = "tcp.timeout", type = int.class, description = "The TCP timeout to use. If the Selenium Client does not respond within this period of time, the request is aborted, and a SocketTimeoutException will be raised.", required = false, defaultValue = "5000"),
        @ConfigProperty(name = "type.safemode", type = boolean.class, description = "If true, elements are clicked, and active element is used for typing (instead of directly sending keys to element).", defaultValue = "false", required = false),
        @ConfigProperty(name = "zindex.check.enabled", type = boolean.class, description = "If true, a z-index check is performed before any element interaction is performed. This ensures the element is 'in foreground'. As this can cause performance decrease, you can disable it, but you may miss errors where web elements are covered by other elements.", defaultValue = "true", required = false),
        @ConfigProperty(name = "phantomjs.init.script", type = String.class, description = "The path and name of a JavaScript file with initialization code for PhantomJS (see PhantomJS API for possible operations).", required = false),
        @ConfigProperty(name = "auto.wait.for.ajax.framework", type = String.class, description = "The name of an AJAX framework to check for pending operations automatically when checking if SUT is busy. Supported framework names are jquery,primefaces,icefaces,dojoPre17. If not set, no automatic check for AJAX operations is performed.", required = false),
        @ConfigProperty(name = "type.wait.before.tab", type = int.class, description = "Number of milliseconds to wait before tabbing out of an input component after typing text. This helps with applications doing lots of Javascript stuff after onkeypress etc.", defaultValue = "0", required = false),
        @ConfigProperty(name = "additional.selenium.headers", type = String.class, description = "Semicolon-separated list of Name-Value pairs (with an equal sign between name and value) of additional headers to send to the Selenium server(s) (NOT to the System Under Test!) on session creation ONLY. This can e.g. be used to give AludraTest Cloud Manager additional hints about the request.", defaultValue = "", required = false) })
public class AludraSelenium2 extends AbstractConfigurableAludraService implements AludraWebGUI {

    private Selenium2Interaction interaction;
    private Selenium2Verification verification;
    private Selenium2Condition condition;

    /** The {@link Selenium2Wrapper} to perform the actual invocations. */
    private Selenium2Wrapper seleniumWrapper;

    private SeleniumWrapperConfiguration configuration;

    @Requirement(hint = "default")
    private GUIComponentFactory componentFactory;

    @Requirement
    private SeleniumWebDriverFactory webDriverFactory;

    private boolean componentFactoryConfigured;

    @Override
    public String getPropertiesBaseName() {
        return "webgui";
    }

    @Override
    public void configure(Preferences preferences) {
        configuration = new SeleniumWrapperConfiguration(preferences);
    }

    @Override
    public GUIComponentFactory getComponentFactory() {
        if (!componentFactoryConfigured) {
            ComponentId<AludraWebGUI> componentId = ComponentId.create(AludraWebGUI.class,
                    aludraServiceContext.getInstanceName());
            ((DefaultGUIComponentFactory) componentFactory).configureForGUIService(aludraServiceContext, componentId);
            componentFactoryConfigured = true;
        }
        return componentFactory;
    }

    /** Used by the framework to configure the service */
    @Override
    public void initService() {
        seleniumWrapper = new Selenium2Wrapper(configuration, getSeleniumResourceService(), webDriverFactory);
        interaction = new Selenium2Interaction(seleniumWrapper);
        verification = new Selenium2Verification(seleniumWrapper);
        condition = new Selenium2Condition(seleniumWrapper);
    }

    private SeleniumResourceService getSeleniumResourceService() {
        return aludraServiceContext.newComponentInstance(SeleniumResourceService.class);
    }

    @Override
    public String getDescription() {
        return "Using Selenium host: " + seleniumWrapper.getUsedSeleniumHost() + ", AUT: " + configuration.getUrlOfAut();
    }

    @Override
    public WebGUIInteraction perform() {
        return this.interaction;
    }

    /** @see AludraWebGUI#verify() */
    @Override
    public WebGUIVerification verify() {
        return this.verification;
    }

    /** @see AludraWebGUI#check() */
    @Override
    public WebGUICondition check() {
        return this.condition;
    }

    @Override
    public void close() {
        try {
            seleniumWrapper.tearDown();
        }
        catch (Exception e) {
            LoggerFactory.getLogger(AludraSelenium2.class).warn("Exception when closing Selenium service", e);
        }
    }

}
