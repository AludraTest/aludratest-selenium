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
package org.aludratest.service.gui.web.selenium;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.aludratest.exception.AutomationException;
import org.aludratest.service.Implementation;

/** A very simple implementation of the {@link SeleniumResourceService} interface. It uses a list of Selenium URLs from
 * <code>selenium.properties</code>, which are iterated round-robin-wise. Note that a single Selenium URL could serve more than
 * one running test, e.g. in the following scenario:
 * <ol>
 * <li>AludraTest uses 2 threads (configured in <code>aludratest.properties</code>) for executing 3 test cases.</li>
 * <li>2 URLs are configured in <code>selenium.properties</code></li>
 * <li>First thread gets first URL, second thread gets second URL</li>
 * <li>Second thread finishes before the first one</li>
 * <li>Second thread gets (also!) first URL for executing third test case.</li>
 * </ol>
 * To prevent this, use the implementation {@link SingleSessionSeleniumResourceService} instead (configure your
 * <code>aludraservice.properties</code> accordingly).
 *
 * @author falbrech */
@Implementation({ SeleniumResourceService.class })
public class SimpleSeleniumResourceService extends AbstractSeleniumResourceService {

    private List<URL> seleniumUrls = Collections.emptyList();

    private AtomicInteger nextIndex = new AtomicInteger(0);

    @Override
    protected void urlsConfigured(List<URL> seleniumUrls) {
        this.seleniumUrls = seleniumUrls;
    }

    @Override
    public URL acquire() {
        if (seleniumUrls.isEmpty()) {
            throw new AutomationException("No Selenium URLs configured. Cannot retrieve Selenium service.");
        }

        synchronized (nextIndex) {
            URL url = seleniumUrls.get(nextIndex.getAndIncrement());
            if (nextIndex.get() >= seleniumUrls.size()) {
                nextIndex.set(0);
            }
            return url;
        }
    }

    @Override
    public void release(URL server) {
        // nothing to do here
    }

}
