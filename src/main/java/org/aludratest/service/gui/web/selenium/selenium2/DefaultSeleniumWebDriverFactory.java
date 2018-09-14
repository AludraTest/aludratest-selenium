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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aludratest.service.gui.web.selenium.SeleniumWrapperConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.LoggerFactory;

/** Default implementation of the {@link SeleniumWebDriverFactory} interface. Contains special treatments for the most common
 * browser types.
 *
 * @author falbrech */
public class DefaultSeleniumWebDriverFactory implements SeleniumWebDriverFactory {

    private static final String CHROME_AUTOMATION_EXTENSIONS_EXPERIMENTAL_ARG = "--disable-automation-extensions";

    private static final Map<String, Selenium2Driver> INSTANCES = new HashMap<String, Selenium2Driver>();
    static {
        INSTANCES.put("FIREFOX", new Selenium2Driver(FirefoxDriver.class, DesiredCapabilities.firefox()));
        INSTANCES.put("INTERNET_EXPLORER",
                new Selenium2Driver(InternetExplorerDriver.class, DesiredCapabilities.internetExplorer()));
        INSTANCES.put("HTML_UNIT", new Selenium2Driver(HtmlUnitDriver.class, createHtmlUnitCaps()));
        INSTANCES.put("CHROME", new SeleniumChromeDriver(ChromeDriver.class, createChromeCaps()));
        INSTANCES.put("SAFARI", new Selenium2Driver(SafariDriver.class, DesiredCapabilities.safari()));
        INSTANCES.put("PHANTOMJS", new SeleniumPhantomJSDriver(PhantomJSDriver.class, createPhantomJsCaps()));
    }

    @Override
    public WebDriver createLocalWebDriver(SeleniumWrapperConfiguration configuration) {
        String driverName = configuration.getDriverName();

        if (!INSTANCES.containsKey(driverName)) {
            throw new IllegalArgumentException("Unsupported Selenium browser name: " + driverName);
        }

        return INSTANCES.get(driverName).newLocalDriver(configuration);
    }

    @Override
    public WebDriver createRemoteWebDriver(URL seleniumUrl, SeleniumWrapperConfiguration configuration) {
        String driverName = configuration.getDriverName();

        if (!INSTANCES.containsKey(driverName)) {
            throw new IllegalArgumentException("Unsupported Selenium browser name: " + driverName);
        }

        return INSTANCES.get(driverName).newRemoteDriver(seleniumUrl, configuration);
    }

    // private helper methods --------------------------------------------------

    private static DesiredCapabilities createChromeCaps() {
        DesiredCapabilities caps = DesiredCapabilities.chrome();
        ChromeOptions opts = new ChromeOptions();
        caps.setCapability(ChromeOptions.CAPABILITY, opts);
        return caps;
    }

    private static DesiredCapabilities createPhantomJsCaps() {
        DesiredCapabilities caps = DesiredCapabilities.phantomjs();
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        caps.setCapability("phantomjs.cli.args",
                new String[] { "--web-security=no", "--ssl-protocol=any", "--ignore-ssl-errors=yes" });
        return caps;
    }

    private static DesiredCapabilities createHtmlUnitCaps() {
        DesiredCapabilities caps = DesiredCapabilities.htmlUnit();
        caps.setJavascriptEnabled(true);
        caps.setBrowserName(BrowserType.CHROME);
        return caps;
    }

    private static class Selenium2Driver {
        private final Class<? extends WebDriver> driverClass;
        private final DesiredCapabilities capabilities;

        protected Selenium2Driver(Class<? extends WebDriver> driverClass,
                DesiredCapabilities capabilities) {
            this.driverClass = driverClass;
            this.capabilities = capabilities;
        }

        public WebDriver newLocalDriver(SeleniumWrapperConfiguration configuration) {
            Constructor<?> cstr = null;
            try {
                cstr = driverClass.getConstructor(DesiredCapabilities.class);
            }
            catch (SecurityException e) {
                throw new WebDriverException(e);
            }
            catch (NoSuchMethodException e) {
                try {
                    cstr = driverClass.getConstructor(Capabilities.class);
                }
                catch (SecurityException e1) {
                    throw new WebDriverException(e1);
                }
                catch (NoSuchMethodException e1) {
                    throw new WebDriverException(e1);
                }
            }

            DesiredCapabilities caps = createCapabilitiesForLocal(configuration);

            try {
                return (WebDriver) cstr.newInstance(caps);
            }
            catch (IllegalArgumentException e) {
                throw new WebDriverException(e);
            }
            catch (InstantiationException e) {
                throw new WebDriverException(e);
            }
            catch (IllegalAccessException e) {
                throw new WebDriverException(e);
            }
            catch (InvocationTargetException e) {
                throw new WebDriverException(e);
            }
        }

        public WebDriver newRemoteDriver(URL seleniumUrl, SeleniumWrapperConfiguration configuration) {
            AludraSeleniumHttpCommandExecutor executor = new AludraSeleniumHttpCommandExecutor(seleniumUrl,
                    configuration.getAdditionalSeleniumHeaders());

            DesiredCapabilities caps = createCapabilitiesForRemote(configuration);

            try {
                RemoteWebDriver driver = new RemoteWebDriver(executor, caps);
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            }
            catch (WebDriverException e) {
                LoggerFactory.getLogger(Selenium2Driver.class)
                        .error("Could not create remote web driver. Last remote HTTP response: " + executor.getLastResponse());
                throw e;
            }

        }

        protected DesiredCapabilities createCapabilitiesForLocal(SeleniumWrapperConfiguration configuration) {
            return capabilities;
        }

        protected DesiredCapabilities createCapabilitiesForRemote(SeleniumWrapperConfiguration configuration) {
            return createCapabilitiesForLocal(configuration);
        }

    }

    private static class SeleniumChromeDriver extends Selenium2Driver {

        public SeleniumChromeDriver(Class<? extends WebDriver> driverClass,
                DesiredCapabilities capabilities) {
            super(driverClass, capabilities);
        }

        @Override
        protected DesiredCapabilities createCapabilitiesForLocal(SeleniumWrapperConfiguration configuration) {
            DesiredCapabilities caps = super.createCapabilitiesForLocal(configuration);
            String[] arguments = configuration.getBrowserArguments();

            if (arguments != null && arguments.length > 0) {
                caps = new DesiredCapabilities(caps);
                // this looks strange, but is the only way to avoid having all Threads sharing the same ChromeOptions object
                ChromeOptions opts = (ChromeOptions) createChromeCaps().getCapability(ChromeOptions.CAPABILITY);
                if (opts != null) {
                    List<String> args = new ArrayList<String>(Arrays.asList(arguments));
                    if (args.contains(CHROME_AUTOMATION_EXTENSIONS_EXPERIMENTAL_ARG)) {
                        // translate into experimental option
                        args.remove(CHROME_AUTOMATION_EXTENSIONS_EXPERIMENTAL_ARG);
                        opts.setExperimentalOption("useAutomationExtension", false);
                    }
                    opts.addArguments(args);

                    caps.setCapability(ChromeOptions.CAPABILITY, opts);
                }
            }

            return caps;
        }

    }

    private static class SeleniumPhantomJSDriver extends Selenium2Driver {

        protected SeleniumPhantomJSDriver(Class<? extends WebDriver> driverClass,
                DesiredCapabilities capabilities) {
            super(driverClass, capabilities);
        }

        @Override
        protected DesiredCapabilities createCapabilitiesForLocal(SeleniumWrapperConfiguration configuration) {
            String[] browserArguments = configuration.getBrowserArguments();

            DesiredCapabilities caps = new DesiredCapabilities(super.createCapabilitiesForLocal(configuration));
            String[] args = (String[]) caps.getCapability("phantomjs.cli.args");
            if (args != null && browserArguments != null && browserArguments.length > 0) {
                String[] newArgs = new String[browserArguments.length + args.length];
                System.arraycopy(args, 0, newArgs, 0, args.length);
                System.arraycopy(browserArguments, 0, newArgs, args.length, browserArguments.length);
                caps.setCapability("phantomjs.cli.args", newArgs);
            }

            return caps;
        }

    }
}
