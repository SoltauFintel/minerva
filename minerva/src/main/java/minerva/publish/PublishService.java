package minerva.publish;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import gitper.base.FileService;
import minerva.MinervaWebapp;
import minerva.model.AttachmentsSO;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.user.User;
import ohhtml.toc.HelpKeysForHeading;
import ohhtml.toc.TocEntry;

public class PublishService {
    private final List<String> langs;
    
    public PublishService(String langsStr) {
        langs = new ArrayList<>();
        for (String lang : langsStr.split(",")) {
            langs.add(lang);
        }
        if (langs.isEmpty()) {
            throw new RuntimeException("Parameter lang must not be empty!");
        }
    }

    public File loginAndPublish(String login, String password, String branch) {
        UserSO userSO = new UserSO((User) MinervaWebapp.factory().getBackendService().login(login, password, null));
        WorkspaceSO workspace = userSO.getWorkspace(branch);
        return publish(workspace);
    }

    private File publish(WorkspaceSO workspace) {
        File targetFolder = MinervaWebapp.factory().getWorkFolder("publish");
        FileService.deleteFolder(targetFolder);
        targetFolder.mkdirs();
        String login = workspace.getUser().getLogin();
        String prefix = login + " | " + workspace.getBranch() + " | ";
        Logger.debug(prefix + "Publishing to " + targetFolder.getAbsolutePath() + " ...");
        // root level
        TocEntry root = new TocEntry();
        root.setTitle("root");
        root.setId("root");
        // language level
        Map<String, TocEntry> langRoot = new HashMap<>();
        for (String lang : langs) {
            TocEntry langRootPage = new TocEntry();
            langRootPage.setTitle(lang);
            langRootPage.setId(lang);
            root.getSubpages().add(langRootPage);
            langRoot.put(lang, langRootPage);
        }
        // book level
        for (BookSO book : workspace.getBooks()) {
            if (book.getBook().getType().isPublic()) {
                Map<String, TocEntry> bookPages = new HashMap<>();
                for (String lang : langs) {
                    TocEntry bookPage = new TocEntry();
                    bookPage.setId(book.getBook().getFolder());
                    bookPage.setTitle(book.getBook().getTitle().getString(lang));
                    langRoot.get(lang).getSubpages().add(bookPage);
                    bookPages.put(lang, bookPage);
                }
                // page level
                copyHtmlAndImg(book.getSeiten(), bookPages, targetFolder);
            }
        }
        // save table of contents file
        File tocJson = new File(targetFolder, "toc.json");
        FileService.savePlainTextFile(tocJson, new Gson().toJson(root)); // no pretty print
        Logger.debug(prefix + "saved to " + tocJson.getAbsolutePath());
        // save exclusions file
        File exclusionsFile = new File(targetFolder, "exclusions.txt");
        FileService.savePlainTextFile(exclusionsFile, workspace.getExclusions().get());
        Logger.debug(prefix + "saved to " + exclusionsFile.getAbsolutePath());
        return targetFolder;
    }
    
    private void copyHtmlAndImg(SeitenSO seiten, Map<String, TocEntry> parent, File targetFolder) {
        if (seiten.isEmpty()) {
            return;
        }
        File sourceImgFolder = new File(seiten.get(0).getBook().getFolder(), "img");
        File targetImgFolder = new File(targetFolder, "img");
        Map<String, TocEntry> pages = new HashMap<>();
        for (SeiteSO seite : seiten) {
            boolean copied = false;
            // collect page data
            for (String lang : langs) {
                // "Don't publish empty pages." (hasContent>0) removed during SeiteSichtbar refactoring.
                copied = true;
                TocEntry p = createTocEntry(seite, lang);
                parent.get(lang).getSubpages().add(p);
                pages.put(lang, p);

                // copy .html files
                File src = new File(seite.filenameHtml(lang));
                if (src.isFile()) {
                    FileService.copyFile(src, new File(targetFolder, lang));
                }
            }
            if (copied) {
                // copy images
                File src = new File(sourceImgFolder, seite.getId());
                if (src.isDirectory()) {
                    FileService.copyFiles(src, new File(targetImgFolder, seite.getId()));
                }
                
                new AttachmentsSO(seite).publish(new File(targetFolder, "attachments")); // copy attachments
    
                copyHtmlAndImg(seite.getSeiten(), pages, targetFolder); // recursive
            }
        }
    }

    private TocEntry createTocEntry(SeiteSO seite, String lang) {
        TocEntry p = new TocEntry(); // needed fields: id,subpages,labels,title,helpKeys
        p.setId(seite.getId());
        p.setTitle(seite.getSeite().getTitle().getString(lang));
        p.getLabels().addAll(seite.getSeite().getTags());
        int levels = seite.getSeite().getTocHeadingsLevels();
        if (levels > 0) {
            p.getLabels().add("toc-h-" + levels);
        }
        levels = seite.getSeite().getTocSubpagesLevels();
        if (levels > 0) {
            p.getLabels().add("toc-s-" + levels);
        }
        p.getHelpKeys().addAll(seite.getSeite().getHelpKeys());
        if (seite.getSeite().getHkh() != null) {
            for (HelpKeysForHeading i : seite.getSeite().getHkh()) {
                String hk = i.getHelpKeys().stream().collect(Collectors.joining(","));
                String e = "$$$" + i.getLanguage() + ":" + i.getHeading() + "$$$" + hk;
                p.getHelpKeys().add(e);
            }
        }
        p.getLinks().addAll(seite.getSeite().getLinks());
        return p;
    }
}
