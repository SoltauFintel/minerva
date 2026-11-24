package gitper.jenkins;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpRequestBase;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import github.soltaufintel.amalia.rest.REST;

/**
 * Open connection to Jenkins server
 */
public class Jenkins {
    private final JenkinsConfig config;

    public Jenkins(JenkinsConfig config) {
        this.config = config;
    }

    public String get(String path) {
        try {
            String auth = config.getUserLogin() + ":" + config.getAccessToken();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
            if (!path.startsWith("http")) {
                path = config.getUrl() + path;
            }
            System.out.println("crumb address: " + config.getUrl() + "/crumbIssuer/api/json");
            String crumbJson = new REST(config.getUrl() + "/crumbIssuer/api/json").withAuthorization(authHeader).get()
                    .response();
            System.out.println("crumb JSON: " + crumbJson); // XXX
            String crumb = new Gson().fromJson(crumbJson, Crumb.class).getCrumb();
            System.out.println("crumb: " + crumb); // XXX
            System.out.println("path: "+path); // XXX
            String r = new REST(path) {
                @Override
                protected void initRequest(HttpRequestBase request) {
                    super.initRequest(request);
                    request.setHeader("Authorization", authHeader);
                    request.setHeader("Jenkins-Crumb", crumb);
                }
            }.get().response();
            System.out.println("------------------------------------------- r:");
            System.out.println(r);
            return r;
        } catch (Exception e) {
            Logger.error("Error accessing " + path);
            Logger.error(e);
            throw new RuntimeException("Error accessing Jenkins");
        }
    }
    
    public String getUrl() {
        return config.getUrl();
    }
    
    public static class Crumb {
        private String crumb;

        public String getCrumb() {
            return crumb;
        }

        public void setCrumb(String crumb) {
            this.crumb = crumb;
        }
    }
}
