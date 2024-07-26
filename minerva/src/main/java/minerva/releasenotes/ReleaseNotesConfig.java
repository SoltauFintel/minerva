package minerva.releasenotes;

import java.util.ArrayList;
import java.util.List;

import minerva.config.MinervaOptions;

public class ReleaseNotesConfig {
    /** customer display text, no technical meaning */
    private String customer;
    /** Jira ticket prefix without "-" */
    private String ticketPrefix;
    /** en, de */
    private String language;
    
    public static ReleaseNotesConfig get(String customer) {
        return loadReleaseNotesConfigs().stream()
                .filter(c -> c.getTicketPrefix().equals(customer)).findFirst().orElse(null);
    }

    public static List<ReleaseNotesConfig> loadReleaseNotesConfigs() {
        List<ReleaseNotesConfig> ret = new ArrayList<>();
        String lines = MinervaOptions.RELEASE_NOTES_CUSTOMERS.get();
        for (String line : lines.split("\n")) {
            String[] w = line.split(",");
            ReleaseNotesConfig e = new ReleaseNotesConfig();
            e.setLanguage(w[0].trim());
            e.setTicketPrefix(w[1].trim());
            e.setCustomer(w[2].trim());
            ret.add(e);
        }
        ret.sort((a, b) -> a.getCustomer().compareToIgnoreCase(b.getCustomer()));
        return ret;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getTicketPrefix() {
        return ticketPrefix;
    }

    public void setTicketPrefix(String ticketPrefix) {
        this.ticketPrefix = ticketPrefix;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
