package minerva.releasenotes;

import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.config.MinervaOptions;

public class ReleaseNotesConfig {
    /** customer display text, no technical meaning */
    private String customer;
    /** Jira ticket prefix without "-" */
    private String ticketPrefix;
    /** en, de */
    private String language;

    public static List<ReleaseNotesConfig> load() {
        List<ReleaseNotesConfig> ret = new ArrayList<>();
        String lines = MinervaOptions.RELEASE_NOTES_CUSTOMERS.get();
        for (String line : lines.split("\n")) {
            String[] w = line.split(",");
            try {
                ret.add(new ReleaseNotesConfig(w[0].trim(), w[1].trim(), w[2].trim()));
            } catch (Exception e) {
                Logger.error(e);
                throw new RuntimeException("Release Notes configuration format error for line: " + line);
            }
        }
        ret.sort((a, b) -> a.getCustomer().compareToIgnoreCase(b.getCustomer()));
        return ret;
    }
    
    public static ReleaseNotesConfig get(String customer) {
        return load().stream().filter(c -> c.getTicketPrefix().equals(customer)).findFirst().orElse(null);
    }

    public ReleaseNotesConfig(String customer, String ticketPrefix, String language) {
        this.customer = customer;
        this.ticketPrefix = ticketPrefix;
        this.language = language;
    }

    public String getCustomer() {
        return customer;
    }

    public String getTicketPrefix() {
        return ticketPrefix;
    }

    public String getLanguage() {
        return language;
    }
}
