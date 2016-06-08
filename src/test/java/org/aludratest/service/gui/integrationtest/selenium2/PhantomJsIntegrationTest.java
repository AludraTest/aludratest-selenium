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
