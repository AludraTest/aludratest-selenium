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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.aludratest.service.gui.integrationtest.GUITest;
import org.junit.Test;

public class PhantomJsIntegrationTest extends GUITest {

    String prevEnv;

    @Override
    public void setUp() throws Exception {
        prevEnv = System.setProperty("aludraTest.environment", "PHANTOMJS");
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (prevEnv != null) {
            System.setProperty("aludraTest.environment", prevEnv);
        }
        else {
            System.clearProperty("aludraTest.environment");
        }
    }

    @Test
    public void testPhantomJsInit() {
        // test that file has been created by init script (script has been called)
        File f = new File("target/phantomjs-init.marker");
        assertTrue(f.isFile());
        f.delete();
    }

}
