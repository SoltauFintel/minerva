package minerva.seite;

import java.util.List;

import minerva.model.SeiteSO;
import ohhtml.toc.HelpKeysForHeading;
import ohhtml.toc.IPage;

public class SeiteIPageAdapter implements IPage {
    private final SeiteSO seite;
    
    public SeiteIPageAdapter(SeiteSO seite) {
        this.seite = seite;
    }

    @Override
    public List<String> getHeadingHelpKeys(String lang, String headingTitle) {
        return seite.getHeadingHelpKeys(lang, headingTitle);
    }

    @Override
    public String getContent(String lang) {
        return seite.getContent().getString(lang);
    }

    @Override
    public List<HelpKeysForHeading> getHkh() {
        return seite.getSeite().getHkh();
    }

    @Override
    public void clearHkh() {
        seite.getSeite().setHkh(null);
    }

    @Override
    public int getTocHeadingsLevels() {
        return seite.getSeite().getTocHeadingsLevels();
    }
}
