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
package org.aludratest.service.gui.integrationtest;

import java.io.ByteArrayInputStream;

import org.aludratest.service.gui.component.FileField;
import org.aludratest.service.gui.web.AludraWebGUI;
import org.aludratest.testcase.TestStatus;
import org.databene.commons.Encodings;
import org.junit.Test;

/** Tests the {@link FileField}-related features of {@link AludraWebGUI} services.
 * @author Volker Bergmann */
@SuppressWarnings("javadoc")
public abstract class AbstractFileFieldTest extends GUITest {

    @Test
    public void setResourceNameAndContent() throws Exception {
        guiTestUIMap.fileField().assertTextEquals("");
        ByteArrayInputStream content = new ByteArrayInputStream("myTestFileContent".getBytes(Encodings.UTF_8));
        guiTestUIMap.fileField().setResourceNameAndContent("myTestFile.txt", content);
        checkLastStepStatus(TestStatus.PASSED);
        guiTestUIMap.fileSubmitButton().click();
        checkLastStepStatus(TestStatus.PASSED);
        guiTestUIMap.fileNameLabel().assertTextEquals("myTestFile.txt");
        checkLastStepStatus(TestStatus.PASSED);
        guiTestUIMap.fileContentLabel().assertTextEquals("myTestFileContent");
        checkLastStepStatus(TestStatus.PASSED);
    }

}
