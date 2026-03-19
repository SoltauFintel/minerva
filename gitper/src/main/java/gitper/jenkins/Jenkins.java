package gitper.jenkins;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.pmw.tinylog.Logger;

/**
 * Open connection to Jenkins server
 */
public class Jenkins {
    private final JenkinsConfig config;
    // https://wiki.jenkins.io/display/JENKINS/Authenticating+scripted+clients --> Java example with httpclient 4.3.x
    private final HttpHost host;
    private final AuthCache authCache;
    private final CloseableHttpClient httpClient;

    public Jenkins(JenkinsConfig config) {
        this.config = config;
        URI uri = URI.create(config.getUrl());
        host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
                new UsernamePasswordCredentials(config.getUserLogin(), config.getAccessToken()));
        authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);
        httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
    }

    public String get(String path) {
        try {
            if (!path.startsWith(config.getUrl())) {
                path = config.getUrl() + path;
            }
            HttpGet httpGet = new HttpGet(path);
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);
            HttpResponse response = httpClient.execute(host, httpGet, localContext);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            Logger.error("Error accessing " + path);
            Logger.error(e);
            throw new RuntimeException("Error accessing Jenkins");
        }
    }
    
    public String getUrl() {
        return config.getUrl();
    }
}
