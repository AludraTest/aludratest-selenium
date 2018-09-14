package org.aludratest.service.gui.web.selenium.selenium2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
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
        opts.addArguments("--disable-extensions");
        opts.setExperimentalOption("useAutomationExtension", false);
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
                    opts.addArguments(arguments);
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
