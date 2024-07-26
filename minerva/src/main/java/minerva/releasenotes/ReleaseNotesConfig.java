package minerva.releasenotes;

import minerva.MinervaWebapp;

public class ReleaseNotesConfig {
    /** customer display text, no technical meaning */
    private String customer;
    /** Jira ticket prefix without "-" */
    private String ticketPrefix;
    /** en, de */
    private String language;
    
    public static ReleaseNotesConfig get(String customer) {
        return MinervaWebapp.factory().getConfig().loadReleaseNotesConfigs().stream()
                .filter(c -> c.getTicketPrefix().equals(customer)).findFirst().orElse(null);
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
