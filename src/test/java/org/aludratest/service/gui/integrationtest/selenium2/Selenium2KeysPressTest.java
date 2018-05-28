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
