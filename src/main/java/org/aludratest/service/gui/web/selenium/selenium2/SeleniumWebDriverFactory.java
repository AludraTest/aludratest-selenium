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
