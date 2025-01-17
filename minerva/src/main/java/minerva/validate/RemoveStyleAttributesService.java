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
import gitper.base.StringService;
import minerva.MinervaWebapp;
import minerva.config.MinervaOptions;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.StatesSO;
import minerva.model.WorkspaceSO;

/**
 * Wenn Seiten aus anderen Quellen nach Minerva kopiert werden, enthält das HTML unschöne style Attribute, sog. CSS Schmutz.
 * Dieser Service entfernt alle style Attribute für einen Workspace.
 */
public class RemoveStyleAttributesService {
    private final List<String> langs = MinervaWebapp.factory().getLanguages();
    private final Map<String, String> files = new HashMap<>();
    private boolean dirty;
    
    public void removeStyleAttributes(WorkspaceSO workspace) {
        String info = workspace.getBranch() + " | Removing style attributes";
        Logger.info(info + "...");
        
        files.clear();
        List<String> ops = new ArrayList<>();
        for (BookSO book : workspace.getBooks()) {
            process(book.getSeiten(), ops);
        }
        if (files.isEmpty()) {
            Logger.info(info + ": no changes");
        } else {
            if (files.size() > 1) {
                Logger.info(info + ": saving " + files.size() + " changed files...");
            }
            File log = new File(MinervaWebapp.factory().getConfig().getWorkspacesFolder() + "/remove-style-attributes-"
                    + workspace.getBranch() + "-" + StringService.now() + ".log");
            FileService.savePlainTextFile(log, ops.stream().collect(Collectors.joining("\r\n")));
            workspace.dao().saveFiles(files, new CommitMessage("style attributes removed"), workspace);
            Logger.info(info + ": style attributes log file: " + log.getAbsolutePath());
            files.clear();
            workspace.pull();
        }
    }
    
    private void process(SeitenSO seiten, List<String> ops) {
        for (SeiteSO seite : seiten) {
            for (String lang : langs) {
                process(seite, lang, ops);
            }
            process(seite.getSeiten(), ops); // recursive
        }
    }
  
    // TODO beim Speichern aufrufen
    /**
     * @param seite page containing HTML
     * @param lang language
     * @param ops operations log, can be null
     * @return true: HTML changed, changes added to getFiles() map
     */
    public boolean process(SeiteSO seite, String lang, List<String> ops) {
        String html = seite.getContent().getString(lang);
        if (StringService.isNullOrEmpty(html)) {
            return false;
        }
        html = filter(html, seite.getTitle() + ", #" + seite.getId(), ops);
        if (dirty) {
            seite.getContent().setString(lang, html);
            seite.saveHtmlTo(files, List.of(lang));
        }
        return dirty;
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
            if (ops != null) {
                ops.add("\n" + title);
                for (String value : values) {
                    ops.add("\t" + value);
                }
            }
            return document.html();
        }
        return html;
    }
    
    public Map<String, String> getFiles() {
        return files;
    }

    public static void start() {
        if (MinervaWebapp.factory().isCustomerVersion()) {
            // TODO Wo krieg ich den Workspace her?
        } else {
            String branches = MinervaOptions.CLEANUP_BRANCHES.get();
            for (WorkspaceSO workspace : StatesSO.getWorkspacesForTimer(branches)) {
                new RemoveStyleAttributesService().removeStyleAttributes(workspace);
            }
        }
    }
}
