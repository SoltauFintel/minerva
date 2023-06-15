package minerva.seite.link;

import minerva.model.SeiteSO;

/**
 * Check All Links: Broken Link
 */
public class CALBrokenLink {
    private final Link link;
    private final SeiteSO seite;

    public CALBrokenLink(Link link, SeiteSO seite) {
        this.link = link;
        this.seite = seite;
    }

    public Link getLink() {
        return link;
    }

    public SeiteSO getSeite() {
        return seite;
    }
}
