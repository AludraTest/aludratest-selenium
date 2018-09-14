package org.aludratest.service.gui.web.selenium;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.aludratest.exception.AutomationException;
import org.aludratest.service.Implementation;

/** An implementation of the {@link SeleniumResourceService} interface which uses a list of Selenium URLs like
 * {@link SimpleSeleniumResourceService} does, but does not return the same URL while it is already in use. This is useful if you
 * really only want to see one test case at once being executed per Selenium Host. If a Thread tries to {@link #acquire()} a
 * resource when all resources are used, it is forced to wait until a resource gets {@link #release(URL)}d.
 *
 * @author falbrech */
@Implementation({ SeleniumResourceService.class })
public class SingleSessionSeleniumResourceService extends AbstractSeleniumResourceService {

    private ArrayBlockingQueue<URL> seleniumUrls;

    private boolean urlsConfigured;

    @Override
    protected void urlsConfigured(List<URL> seleniumUrls) {
        this.urlsConfigured = !seleniumUrls.isEmpty();
        if (this.urlsConfigured) {
            this.seleniumUrls = new ArrayBlockingQueue<URL>(seleniumUrls.size(), true);
            this.seleniumUrls.addAll(seleniumUrls);
        }
    }

    @Override
    public URL acquire() {
        if (!urlsConfigured) {
            throw new AutomationException("No Selenium URLs configured. Cannot retrieve Selenium service.");
        }
        try {
            return seleniumUrls.take();
        }
        catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public void release(URL server) {
        seleniumUrls.add(server);
    }

}
