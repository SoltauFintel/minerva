package minerva.seite.helpkeys;

import java.util.List;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.web.action.Escaper;
import ohhtml.toc.TocMacro;
import ohhtml.toc.TocMacroPage;

public class VSPTocMacro extends TocMacro {

    public VSPTocMacro(TocMacroPage page, String lang) {
        super(page, "-", lang, "");
    }
    
    // Display of help keys can be too long. That would make the ViewSeitePage much too wide.
    @Override
    protected String formatHeadingHelpKeys(List<String> helpKeys) { // will only be called if helpKeys is not empty
        String ret = limit(helpKeys.get(0));
        return helpKeys.size() > 1 ? ret + ", ..." : ret;
    }
    
    @Override
    protected String makeHeading(List<String> helpKeys, int lfd) {
        String ret = super.makeHeading(helpKeys, lfd);
        if (helpKeys.size() > 1) {
            ret = ret.replace("<a ", "<a title=\"" + Escaper.esc(getHelpKeysText() + ": " + //
                    helpKeys.stream().map(hk -> limit(hk)).collect(Collectors.joining(", "))) + "\" ");
        }
        return ret;
    }
    
    private String limit(String helpKey) {
        final int max = 60;
        if (helpKey.length() > max + "...".length()) {
            helpKey = helpKey.substring(0, max) + "...";
        }
        return helpKey;
    }
}
