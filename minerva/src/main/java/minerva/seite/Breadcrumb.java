package minerva.seite;

import minerva.base.NlsString;

public class Breadcrumb {
    private String link;
    private NlsString title;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public NlsString getTitle() {
        return title;
    }

    public void setTitle(NlsString title) {
        this.title = title;
    }
}
