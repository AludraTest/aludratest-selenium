package org.aludratest.service.gui.integrationtest.selenium2;

import org.aludratest.service.gui.integrationtest.GUITest;
import org.aludratest.testcase.TestStatus;
import org.junit.Test;

/**
 * Tests Keys Press feature with Selenium 2.
 * @author vdorai
 *
 */
public class Selenium2KeysPressTest  extends GUITest{
    /**
     * Focus on first check box and on tab, focus on second check box.
     * on Shift+tab focus returns to first check box.
     */
    @Test
    public void assertPressKeysFocus() {
        guiTestUIMap.firstCheckBox().focus();
        guiTestUIMap.firstCheckBox().assertFocus();
        checkLastStepStatus(TestStatus.PASSED);
        guiTestUIMap.pressKeys("\uE004");
        guiTestUIMap.secondCheckBox().assertFocus();
        checkLastStepStatus(TestStatus.PASSED);
        guiTestUIMap.pressKeys("\uE008","\uE004");
        guiTestUIMap.firstCheckBox().assertFocus();
        checkLastStepStatus(TestStatus.PASSED);
    }
}
