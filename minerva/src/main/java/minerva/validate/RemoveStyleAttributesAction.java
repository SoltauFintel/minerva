package minerva.validate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import gitper.access.CommitMessage;
import gitper.base.FileService;
import minerva.MinervaWebapp;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.workspace.WAction;

/**
 * Sonderaktion: style Attribute aus den Seiten (HTML) entfernen,
 * denn solche Attribute stellen stets CSS Schmutz dar.
 */
public class RemoveStyleAttributesAction extends WAction {
    private final List<String> langs = MinervaWebapp.factory().getConfig().getLanguages();
    private Map<String, String> files = new HashMap<>();
    private boolean dirty;
    
    @Override
    protected void execute() {
        user.onlyAdmin();
        Logger.info("Removing style attributes... | branch: " + branch);
        
        files.clear();
        List<String> ops = new ArrayList<>();
        for (BookSO book : workspace.getBooks()) {
            Logger.info("  - book: " + book.getTitle());
            process(book.getSeiten(), ops);
        }
        if (files.size() > 1) {
            Logger.info("  - saving " + files.size() + " changed files...");
        }
        File log = new File(MinervaWebapp.factory().getConfig().getWorkspacesFolder() + "/remove-style-attributes.log");
        FileService.savePlainTextFile(log, ops.stream().collect(Collectors.joining("\r\n")));
        workspace.dao().saveFiles(files, new CommitMessage("style attributes removed"), workspace);
        Logger.info("All style attributes removed. | style attributes log file: " + log.getAbsolutePath());
        files.clear();
        workspace.pull();

        ctx.redirect("/w/" + branch + "/menu");
    }
    
    private void process(SeitenSO seiten, List<String> ops) {
        for (SeiteSO seite : seiten) {
            for (String lang : langs) {
                String html = seite.getContent().getString(lang);
                if (html != null && !html.isEmpty()) {
                    html = filter(html, seite.getTitle() + ", #" + seite.getId(), ops);
                    if (dirty) {
                        seite.getContent().setString(lang, html);
                        seite.saveHtmlTo(files, List.of(lang));
                    }
                }
            }
            process(seite.getSeiten(), ops); // recursive
        }
    }

    private String filter(String html, String title, List<String> ops) {
        Document document = Jsoup.parse(html);
        Elements elementsWithStyle = document.select("[style]");
        dirty = !elementsWithStyle.isEmpty();
        if (dirty) {
            Set<String> values = new TreeSet<>();
            for (Element element : elementsWithStyle) {
                values.add(element.attr("style"));
                element.removeAttr("style");
            }
            ops.add("\n" + title);
            for (String value : values) {
                ops.add("\t" + value);
            }
            return document.html();
        }
        return html;
    }
}
