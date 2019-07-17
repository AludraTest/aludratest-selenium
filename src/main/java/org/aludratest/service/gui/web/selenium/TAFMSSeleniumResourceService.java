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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aludratest.config.AludraTestConfig;
import org.aludratest.config.MutablePreferences;
import org.aludratest.config.Preferences;
import org.aludratest.exception.AutomationException;
import org.aludratest.service.Implementation;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.plexus.component.annotations.Requirement;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Uses a TAFMS server to obtain Selenium resources. The TAFMS server deals with priorities among different users.
 *
 * @author falbrech */
@Implementation({ SeleniumResourceService.class })
public class TAFMSSeleniumResourceService extends AbstractSeleniumResourceService {

    private static final Logger LOG = LoggerFactory.getLogger(TAFMSSeleniumResourceService.class);
    private static final String REQUEST_ID = "requestId";

    @Requirement
    private AludraTestConfig aludraConfig;

    private Preferences configuration;

    private Map<String, String> hostResourceIds = new ConcurrentHashMap<>();

    @Override
    protected void urlsConfigured(List<URL> seleniumUrls) {
        // the tafms url is used from the config
    }

    @Override
    public URL acquire() {

        // prepare a JSON query to the given TAFMS server
        JSONObject query = createJsonQuery();

        // prepare authentication
        CloseableHttpClient client = createCloseableHttpClient();

        String message = null;
        try {
            BasicHttpContext localcontext = createBasicHttpContext();

            do {
                // send a POST request to resource URL
                HttpPost request = new HttpPost(getTafmsUrl() + "resource");

                // attach query as JSON string data
                request.setEntity(new StringEntity(query.toString(), ContentType.APPLICATION_JSON));

                // fire request and save response
                CloseableHttpResponse response = client.execute(request, localcontext);

                try {

                    checkForClientProtocalException(response);

                    message = extractMessage(response);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        LOG.error("Exception when querying TAFMS server for resource. HTTP Status: {} + message: {}", response.getStatusLine().getStatusCode(), message);
                        return null;
                    }

                    JSONObject object = new JSONObject(message);
                    if (object.has("errorMessage")) {
                        LOG.error("TAFMS server reported an error: {}", object.get("errorMessage"));
                        return null;
                    }

                    // continue wait?
                    if (object.has("waiting") && object.getBoolean("waiting")) {
                        query.put(REQUEST_ID, object.getString(REQUEST_ID));
                    }
                    else {
                        JSONObject resource = object.optJSONObject("resource");
                        if (resource == null) {
                            LOG.error("TAFMS server response did not provide a resource. Message was: {}", message);
                            return null;
                        }

                        String sUrl = resource.getString("url");
                        hostResourceIds.put(sUrl, object.getString(REQUEST_ID));

                        return new URL(sUrl);
                    }
                }
                finally {
                    IOUtils.closeQuietly(response);
                }
            }
            while (true);

        }
        catch (IOException e) {
            LOG.error("Exception in communication with TAFMS server", e);
            return null;
        }
        catch (JSONException e) {
            LOG.error("Invalid JSON received from TAFMS server. JSON message was: " + message, e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(client);
        }
    }

    private void checkForClientProtocalException(CloseableHttpResponse response) throws ClientProtocolException {
        if (response.getStatusLine() == null) {
            throw new ClientProtocolException("No HTTP status line transmitted");
        }
    }

    private BasicHttpContext createBasicHttpContext() throws MalformedURLException {
        // use preemptive authentication to avoid double connection count
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        URL url = new URL(getTafmsUrl());
        HttpHost host = new HttpHost(url.getHost(), url.getPort() == -1 ? url.getDefaultPort() : url.getPort(),
                url.getProtocol());
        authCache.put(host, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
        return localcontext;
    }


    private CloseableHttpClient createCloseableHttpClient() {
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(configuration.getStringValue("tafms.user"),
                configuration.getStringValue("tafms.password")));

        return HttpClientBuilder.create().setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                .disableConnectionState().disableAutomaticRetries().setDefaultCredentialsProvider(provider).build();
    }

    private JSONObject createJsonQuery() {
        JSONObject query = new JSONObject();

        try {
            query.put("resourceType", "selenium");
            query.put("niceLevel", configuration.getIntValue("tafms.niceLevel", 0));
            String jobName = configuration.getStringValue("tafms.jobName");
            if (jobName != null && !"".equals(jobName)) {
                query.put("jobName", jobName);
            }
        }
        catch (JSONException e) {
            LOG.warn("Error trying to parse JSON for query", e);
        }
        return query;
    }

    @Override
    public void release(URL server) {
        if (server == null) {
            return;
        }

        String resourceKey = hostResourceIds.remove(server);
        if (resourceKey == null) {
            return;
        }

        CloseableHttpClient client = HttpClientBuilder.create().setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                .disableConnectionState().disableAutomaticRetries().build();

        // send a DELETE request to resource URL
        HttpDelete request = new HttpDelete(getTafmsUrl() + "resource/" + resourceKey);
        CloseableHttpResponse response = null;

        try {
            response = client.execute(request);
        }
        catch (IOException e) {
            LOG.warn("Could not release TAFMS resource", e);
        }
        finally {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(client);
        }
    }

    public int getHostCount() {
        // Just return number of configured Threads, as resource is "shared" and number can vary
        return aludraConfig.getNumberOfThreads();
    }

    @Override
    public void fillDefaults(MutablePreferences preferences) {
        // was always empty
    }

    @Override
    public void configure(Preferences preferences) {
        int niceLevel = preferences.getIntValue("tafms.niceLevel", 0);
        if (niceLevel < -20 || niceLevel > 19) {
            throw new AutomationException("Illegal value for tafms.niceLevel: " + niceLevel
                    + ". Value must be from -20 to +19, inclusive");
        }

        String url = preferences.getStringValue("tafms.url");
        try {
            new URL(url);
        }
        catch (Exception e) {
            throw new AutomationException("Illegal URL for tafms.url: " + url, e);
        }

        String user = preferences.getStringValue("tafms.user");
        if (user == null || "".equals(user)) {
            throw new AutomationException("TAFMS user name is missing");
        }
        String password = preferences.getStringValue("tafms.password");
        if (password == null || "".equals(password)) {
            throw new AutomationException("TAFMS password is missing");
        }

        configuration = preferences;
    }

    private String getTafmsUrl() {
        String url = configuration.getStringValue("tafms.url");
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    private String extractMessage(HttpResponse response) throws IOException {
        if (response.getEntity() == null) {
            return null;
        }

        HttpEntity entity = response.getEntity();
        InputStream in = entity.getContent();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            IOUtils.copy(in, baos);
            return new String(baos.toByteArray(), "UTF-8");
        }
        finally {
            EntityUtils.consumeQuietly(entity);
            IOUtils.closeQuietly(in);
        }
    }

}