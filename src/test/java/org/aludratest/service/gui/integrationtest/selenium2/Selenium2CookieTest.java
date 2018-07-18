package org.aludratest.service.gui.integrationtest.selenium2;

import org.aludratest.service.gui.integrationtest.GUITest;
import org.junit.Test;

/**
 * Tests add/remove cookie feature with Selenium 2.
 * @author vdorai
 *
 */
public class Selenium2CookieTest extends GUITest {
    /**
     * Create cookie and add.
     */
    @Test
    public void assertAddCookie() {
        guiTestUIMap.addCookie("MONI_SYNTHE_ACTION", "FIND", ".hamburgsud.com", "/", 60*60);
    }
    
    /**
     * Delete cookie.
     */
    @Test
    public void assertDeleteCookieNamed() {
        guiTestUIMap.deleteCookieNamed("MONI_SYNTHE_ACTION");
    }
}
