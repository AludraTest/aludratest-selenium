package org.aludratest.service.gui.integrationtest.selenium2;

import java.util.Calendar;
import java.util.Date;

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
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
        guiTestUIMap.addCookie("MONI_SYNTHE_ACTION", "FIND", ".hamburgsud.com", "/", cal.getTime());
    }
    
    /**
     * Delete cookie.
     */
    @Test
    public void assertDeleteCookieNamed() {
        guiTestUIMap.deleteCookieNamed("MONI_SYNTHE_ACTION");
    }
}
