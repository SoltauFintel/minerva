package minerva.releasenotes;

public class ReleaseNotesConfig {
    private String customer;
    private String ticketPrefix;
    @Deprecated
    private String spaceKey;
    private String language;
    private String rootTitle;

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

    @Deprecated
    public String getSpaceKey() {
        return spaceKey;
    }

    @Deprecated
    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRootTitle() {
        return rootTitle;
    }

    public void setRootTitle(String rootTitle) {
        this.rootTitle = rootTitle;
    }
}
