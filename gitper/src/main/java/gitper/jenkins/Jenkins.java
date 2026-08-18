package gitper.jenkins;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.pmw.tinylog.Logger;

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
            return new REST(path).withAuthorization(authHeader).get().response();
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
