package minerva.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import github.soltaufintel.amalia.web.config.AppConfig;
import minerva.base.StringService;

public class MinervaConfig {
    // TODO Alle AppConfig Zugriffe hier in diese Klasse zentralisieren.
    private final AppConfig config;

    public MinervaConfig(AppConfig config) {
        this.config = config;
    }
    
    public List<String> getLanguages() {
        List<String> languages = new ArrayList<>();
        for (String lang : config.get("languages", "de,en").split(",")) {
            String a = lang.trim().toLowerCase();
            if (languages.contains(a)) {
                throw new ConfigurationException("Duplicate key in setting 'languages'!");
            }
            languages.add(a);
        }
        if (languages.isEmpty() || languages.contains("")) {
            throw new ConfigurationException("Setting 'languages' not ok! Check config!");
        }
        return languages;
    }

    /**
     * @return false: persistence with local file system,
     * true: persistence with local file system and remote Gitlab
     */
    public boolean isGitlab() {
        String backend = System.getenv("BACKEND"); // for setting backend to file-system in IDE mode
        if (StringService.isNullOrEmpty(backend)) {
            backend = config.get("backend");
        }
        return "gitlab".equalsIgnoreCase(backend);
    }
    
    public String getWorkspacesFolder() {
        String ret = get("workspaces");
        if (ret == null || ret.trim().length() <= 3) {
            throw new ConfigurationException("Setting 'workspaces' not ok!");
        }
        ret = ret.trim().replace("\\", "/");
        if (ret.endsWith("/")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        File f = new File(ret);
        f.mkdirs();
        if (!f.isDirectory()) {
            throw new ConfigurationException("Folder for setting 'workspaces' can not be created! "
                    + f.getAbsolutePath());
        }
        return ret;
    }
    
    public String getGitlabUrl() {
        String ret = get("gitlab.url");
        if (isGitlab() && StringService.isNullOrEmpty(ret)) {
            throw new ConfigurationException("Missing setting 'gitlab.url'!");
        }
        return ret;
    }
    
    public String getGitlabProject() {
        String ret = get("gitlab.project");
        if (isGitlab()) {
            if (StringService.isNullOrEmpty(ret)) {
                throw new ConfigurationException("Missing setting 'gitlab.project'!");
            } else if (!ret.contains("/")) {
                throw new ConfigurationException("Setting 'gitlab.project' must contain '/'!");
            }
        }
        return ret;
    }
    
    private String get(String key) {
        return config.get(key);
    }

    public List<String> getPersons() {
        List<String> ret = new ArrayList<>();
        String persons = get("persons");
        if (persons != null) {
            for (String name : persons.split(",")) {
                if (!name.isBlank()) {
                    ret.add(name.trim());
                }
            }
            Collections.sort(ret);
        }
        return ret;
    }
}
