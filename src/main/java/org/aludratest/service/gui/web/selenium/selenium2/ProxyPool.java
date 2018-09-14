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
package org.aludratest.service.gui.web.selenium.selenium2;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.aludratest.service.gui.web.selenium.httpproxy.AuthenticatingHttpProxy;

/** Creates and manages a pool of {@link AuthenticatingHttpProxy} instances.
 *
 * @author Volker Bergmann
 * @author falbrech */
public class ProxyPool {

    private Queue<AuthenticatingHttpProxy> proxies = new ConcurrentLinkedQueue<AuthenticatingHttpProxy>();

    private AtomicInteger nextLocalPort;

    private String targetHost;

    private int targetPort;



    /** Creates a pool of proxies which forward all calls to the same target server and port, but each one listening on a
     * different local port. The used local port numbers begin with 'firstLocalPort' (e.g. 8000) and use the following port
     * numbers (e.g. 8001, 8002, ...)
     * @param targetHost the target host
     * @param targetPortCfg the configured target port
     * @param firstLocalPort the first local port to be opened by the proxies */
    public ProxyPool(String targetHost, int targetPortCfg, int firstLocalPort) {
        this.targetHost = targetHost;
        this.targetPort = (targetPortCfg >= 0 ? targetPortCfg : 80);
        this.nextLocalPort = new AtomicInteger(firstLocalPort);
    }

    /** Acquires a proxy for exclusive use by a single client, creating a new one if necessary.
     * @return A proxy for exclusive use. */
    public AuthenticatingHttpProxy acquire() {
        if (proxies.isEmpty()) {
            proxies.add(new AuthenticatingHttpProxy(nextLocalPort.getAndIncrement(), targetHost, targetPort));
        }
        return proxies.poll();
    }

    /** Puts back a used proxy into the pool. The client has to call this to put its proxy back to the pool after using it.
     * @param proxy The proxy to release */
    public void release(AuthenticatingHttpProxy proxy) {
        proxies.add(proxy);
    }

}
