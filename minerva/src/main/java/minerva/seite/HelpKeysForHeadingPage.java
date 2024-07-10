package minerva.seite;

import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;

/**
 * Edit help keys for headings (Anker Verknüpfungen)
 */
public class HelpKeysForHeadingPage extends SPage {

    @Override
    protected void execute() {
        String lang = ctx.pathParam("lang");
        String h = ctx.pathParam("h"); // lfd. Nr. der Überschrift, vgl. TocMacro
        validateLanguage(lang);
        String headingTitle = getHeadingTitle(lang, Integer.parseInt(h));
        
        if (isPOST()) {
            seite.saveHeadingHelpKeys(lang, headingTitle, ctx.formParam("helpKeys"));
            ctx.redirect(viewlink);
        } else {
            Logger.info(user.getLogin() + " | " + branch + " | " + seite.getSeite(). getTitle().getString(lang) +
                    ": edit help-keys for heading " + lang + "#" + h + ": " + headingTitle);
            
            header(n("helpKeys"));
            put("headingTitle", esc(headingTitle));
            put("h", esc(h));
            put("lang", esc(lang));
            put("helpKeys", seite.getHeadingHelpKeys(lang, headingTitle).stream().collect(Collectors.joining("\n")) + "\n");
        }
    }

    private void validateLanguage(String lang) {
        for (String i : MinervaWebapp.factory().getLanguages()) {
            if (i.equals(lang)) {
                return; // ok
            }
        }
        throw new RuntimeException("Illegal language: " + lang);
    }
    
    private String getHeadingTitle(String lang, int h) {
        Elements headings = TocMacro._getHeadings(Jsoup.parse(seite.getContent().getString(lang)));
        int lfd = 0;
        for (Element heading : headings) {
            if (++lfd == h) {
                return heading.text();
            }
        }
        throw new RuntimeException("Heading ID " + lang + "/" + h + " doesn't exist!");
    }
}
