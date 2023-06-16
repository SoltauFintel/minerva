package minerva.preview;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.seite.IBreadcrumbLinkBuilder;

public class PreviewAreaBreadcrumbLinkBuilder implements IBreadcrumbLinkBuilder {
    private final String customer;
    private final String lang;
    
    public PreviewAreaBreadcrumbLinkBuilder(String customer, String lang) {
        this.customer = Escaper.esc(customer);
        this.lang = Escaper.esc(lang);
    }

    @Override
    public String build(String branch, String folder, String id) {
        String ret = "/p/" + branch + "/" + customer + "/" + folder + "/" + lang;
        if (id != null) {
            ret += "/" + id;
        }
        return ret;
    }
}
