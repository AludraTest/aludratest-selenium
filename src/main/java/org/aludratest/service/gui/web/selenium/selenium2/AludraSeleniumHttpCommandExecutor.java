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

/*
 Copyright 2007-2011 Selenium committers

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import static org.openqa.selenium.remote.DriverCommand.GET_ALL_SESSIONS;
import static org.openqa.selenium.remote.DriverCommand.NEW_SESSION;
import static org.openqa.selenium.remote.DriverCommand.QUIT;

import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LocalLogs;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.NeedsLocalLogs;
import org.openqa.selenium.logging.profiler.HttpProfilerLogEntry;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.HttpSessionId;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.JsonHttpCommandCodec;
import org.openqa.selenium.remote.http.JsonHttpResponseCodec;
import org.openqa.selenium.remote.internal.HttpClientFactory;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/** A full copy of the Selenium HttpCommandExecutor class to be able to use a custom Request timeout.
 * 
 * @author falbrech */
public class AludraSeleniumHttpCommandExecutor implements CommandExecutor, NeedsLocalLogs {

    private static final int MAX_REDIRECTS = 10;

    private static final int THREE_HOURS = (int) TimeUnit.MILLISECONDS.convert(3, TimeUnit.HOURS);

    private final HttpHost targetHost;
    private final URL remoteServer;
    private final HttpClient client;
    private final JsonHttpCommandCodec commandCodec;
    private final JsonHttpResponseCodec responseCodec;

    private static HttpClientFactory httpClientFactory;

    private LocalLogs logs = LocalLogs.getNullLogger();

    private int requestTimeout;

    private HttpResponse lastResponse;

    /** Constructs a new HttpCommandExecutor for the given remote server.
     * 
     * @param addressOfRemoteServer Remove server, or <code>null</code> to fall back to the System property
     *            <code>webdriver.remote.server</code>, or to <code>http://localhost:4444/wd/hub</code> if system property is not
     *            set. */
    public AludraSeleniumHttpCommandExecutor(URL addressOfRemoteServer) {
        try {
            remoteServer = addressOfRemoteServer == null ? new URL(System.getProperty("webdriver.remote.server",
                    "http://localhost:4444/wd/hub")) : addressOfRemoteServer;
        }
        catch (MalformedURLException e) {
            throw new WebDriverException(e);
        }

        commandCodec = new JsonHttpCommandCodec();
        responseCodec = new JsonHttpResponseCodec();

        synchronized (AludraSeleniumHttpCommandExecutor.class) {
            if (httpClientFactory == null) {
                httpClientFactory = new HttpClientFactory();
            }
        }

        if (addressOfRemoteServer != null && addressOfRemoteServer.getUserInfo() != null) {
            // Use HTTP Basic auth
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(addressOfRemoteServer.getUserInfo());
            client = httpClientFactory.createHttpClient(credentials);
        }
        else {
            client = httpClientFactory.getHttpClient();
        }

        // Some machines claim "localhost.localdomain" is the same as "localhost".
        // This assumption is not always true.

        String host = remoteServer.getHost().replace(".localdomain", "");

        targetHost = new HttpHost(host, remoteServer.getPort(), remoteServer.getProtocol());
    }

    /** Sets the request timeout to use, in milliseconds. 0 indicates no custom timeout - Selenium defaults apply (3 hours!).
     * 
     * @param requestTimeout Request timeout to use, in milliseconds, or 0 to disable custom timeout. */
    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    @Override
    public void setLocalLogs(LocalLogs logs) {
        this.logs = logs;
    }

    private void log(String logType, LogEntry entry) {
        logs.addEntry(logType, entry);
    }

    /** Returns the address of the remote server in use.
     * 
     * @return The address of the remote server in use. */
    public URL getAddressOfRemoteServer() {
        return remoteServer;
    }

    public HttpResponse getLastResponse() {
        return lastResponse;
    }

    @Override
    public Response execute(Command command) throws IOException {
        HttpContext context = new BasicHttpContext();

        if (command.getSessionId() == null) {
            if (QUIT.equals(command.getName())) {
                return new Response();
            }
            if (!GET_ALL_SESSIONS.equals(command.getName()) && !NEW_SESSION.equals(command.getName())) {
                throw new SessionNotFoundException("Session ID is null. Using WebDriver after calling quit()?");
            }
        }

        HttpRequest request = commandCodec.encode(command);

        String requestUrl = remoteServer.toExternalForm().replaceAll("/$", "") + request.getUri();

        HttpUriRequest httpMethod = createHttpUriRequest(request.getMethod(), requestUrl);
        for (String name : request.getHeaderNames()) {
            // Skip content length as it is implicitly set when the message entity is set below.
            if (!"Content-Length".equalsIgnoreCase(name)) {
                for (String value : request.getHeaders(name)) {
                    httpMethod.addHeader(name, value);
                }
            }
        }

        if (httpMethod instanceof HttpPost) {
            ((HttpPost) httpMethod).setEntity(new ByteArrayEntity(request.getContent()));
        }

        if (requestTimeout > 0 && (httpMethod instanceof HttpRequestBase)) {
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(15000).setConnectTimeout(15000)
                    .setSocketTimeout(requestTimeout).build();
            ((HttpRequestBase) httpMethod).setConfig(requestConfig);
        }
        else if (httpMethod instanceof HttpRequestBase) {
            // ensure Selenium Standard is set
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000).setConnectTimeout(60000)
                    .setSocketTimeout(THREE_HOURS).build();
            ((HttpRequestBase) httpMethod).setConfig(requestConfig);
        }

        try {
            log(LogType.PROFILER, new HttpProfilerLogEntry(command.getName(), true));
            HttpResponse response = fallBackExecute(context, httpMethod);
            log(LogType.PROFILER, new HttpProfilerLogEntry(command.getName(), false));

            lastResponse = response;
            response = followRedirects(client, context, response, /* redirect count */0);
            lastResponse = response;

            return createResponse(response, context);
        }
        catch (UnsupportedCommandException e) {
            if (e.getMessage() == null || "".equals(e.getMessage())) {
                throw new UnsupportedOperationException("No information from server. Command name was: " + command.getName(),
                        e.getCause());
            }
            throw e;
        }
        catch (SocketTimeoutException e) {
            LoggerFactory.getLogger(AludraSeleniumHttpCommandExecutor.class).warn(
                    "Timeout in HTTP Command Executor. Timeout was "
                            + ((HttpRequestBase) httpMethod).getConfig().getSocketTimeout());
            throw e;
        }
    }

    private static HttpUriRequest createHttpUriRequest(HttpMethod method, String url) {
        switch (method) {
            case DELETE:
                return new HttpDelete(url);
            case GET:
                return new HttpGet(url);
            case POST:
                return new HttpPost(url);
        }
        throw new AssertionError("Unsupported method: " + method);
    }

    private HttpResponse fallBackExecute(HttpContext context, HttpUriRequest httpMethod) throws IOException {
        try {
            return client.execute(targetHost, httpMethod, context);
        }
        catch (BindException e) {
            // If we get this, there's a chance we've used all the local ephemeral sockets
            // Sleep for a bit to let the OS reclaim them, then try the request again.
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException ie) {
                throw Throwables.propagate(ie);
            }
        }
        catch (NoHttpResponseException e) {
            // If we get this, there's a chance we've used all the remote ephemeral sockets
            // Sleep for a bit to let the OS reclaim them, then try the request again.
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException ie) {
                throw Throwables.propagate(ie);
            }
        }
        return client.execute(targetHost, httpMethod, context);
    }

    private HttpResponse followRedirects(HttpClient client, HttpContext context, HttpResponse response, int redirectCount) {
        if (!isRedirect(response)) {
            return response;
        }

        try {
            // Make sure that the previous connection is freed.
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                EntityUtils.consume(httpEntity);
            }
        }
        catch (IOException e) {
            throw new WebDriverException(e);
        }

        if (redirectCount > MAX_REDIRECTS) {
            throw new WebDriverException("Maximum number of redirects exceeded. Aborting");
        }

        String location = response.getFirstHeader("location").getValue();
        URI uri;
        try {
            uri = buildUri(context, location);

            HttpGet get = new HttpGet(uri);
            get.setHeader("Accept", "application/json; charset=utf-8");
            HttpResponse newResponse = client.execute(targetHost, get, context);
            return followRedirects(client, context, newResponse, redirectCount + 1);
        }
        catch (URISyntaxException e) {
            throw new WebDriverException(e);
        }
        catch (ClientProtocolException e) {
            throw new WebDriverException(e);
        }
        catch (IOException e) {
            throw new WebDriverException(e);
        }
    }

    private URI buildUri(HttpContext context, String location) throws URISyntaxException {
        URI uri;
        uri = new URI(location);
        if (!uri.isAbsolute()) {
            HttpHost host = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            uri = new URI(host.toURI() + location);
        }
        return uri;
    }

    private boolean isRedirect(HttpResponse response) {
        int code = response.getStatusLine().getStatusCode();

        return (code == 301 || code == 302 || code == 303 || code == 307) && response.containsHeader("location");
    }

    private Response createResponse(HttpResponse httpResponse, HttpContext context) throws IOException {
        org.openqa.selenium.remote.http.HttpResponse internalResponse = new org.openqa.selenium.remote.http.HttpResponse();

        internalResponse.setStatus(httpResponse.getStatusLine().getStatusCode());
        for (Header header : httpResponse.getAllHeaders()) {
            for (HeaderElement headerElement : header.getElements()) {
                internalResponse.addHeader(header.getName(), headerElement.getValue());
            }
        }

        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            try {
                internalResponse.setContent(EntityUtils.toByteArray(entity));
            }
            finally {
                EntityUtils.consume(entity);
            }
        }

        Response response = responseCodec.decode(internalResponse);
        if (response.getSessionId() == null) {
            HttpHost finalHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            String uri = finalHost.toURI();
            String sessionId = HttpSessionId.getSessionId(uri);
            response.setSessionId(sessionId);
        }

        return response;
    }
}
