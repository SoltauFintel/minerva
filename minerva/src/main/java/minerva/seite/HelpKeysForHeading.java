package minerva.seite;

import java.util.List;

public class HelpKeysForHeading {
    private String language;
    private String heading;
    /**
     * Help keys are trimmed.
     * No empty entries.
     * Commented-out help keys are not planned, but practically possible by prefixing them with characters that break the key.
     */
    private List<String> helpKeys;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public List<String> getHelpKeys() {
        return helpKeys;
    }

    public void setHelpKeys(List<String> helpKeys) {
        this.helpKeys = helpKeys;
    }
}
