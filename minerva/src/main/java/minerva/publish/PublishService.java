package minerva.publish;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.base.FileService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;

public class PublishService {
    private final List<String> langs;
    
    public PublishService(List<String> langs) {
        this.langs = langs;
    }
    
    public static File loginAndPublish(String langsStr, String login, String password, String branch) {
        List<String> langs = new ArrayList<>();
        for (String lang : langsStr.split(",")) {
            langs.add(lang);
        }
        if (langs.isEmpty()) {
            throw new RuntimeException("Parameter lang must not be empty!");
        }

        UserSO userSO = new UserSO(MinervaWebapp.factory().getLoginService().login(login, password));
        WorkspaceSO workspace = userSO.getWorkspace(branch);

        return new PublishService(langs).publish(workspace);
    }

    public File publish(WorkspaceSO workspace) {
        File targetFolder = MinervaWebapp.factory().getWorkFolder("publish");
        FileService.deleteFolder(targetFolder);
        targetFolder.mkdirs();
        String login = workspace.getUser().getLogin();
        Logger.info(login + " | " + workspace.getBranch() + " | Publishing to " + targetFolder.getAbsolutePath() + " ...");
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
        // save table of contents file
        File tocJson = new File(targetFolder, "toc.json");
        FileService.saveJsonFile(tocJson, root);
        Logger.info("saved to " + tocJson.getAbsolutePath());
        FileService.savePlainTextFile(new File(targetFolder, "exclusions.txt"), workspace.getExclusions().get());
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
            // collect page data
            for (String lang : langs) {
                TocEntry p = new TocEntry(); // needed fields: id,subpages,labels,title,helpKeys
                p.setId(seite.getId());
                p.setTitle(seite.getSeite().getTitle().getString(lang));
                p.getLabels().addAll(seite.getSeite().getTags());
                p.getHelpKeys().addAll(seite.getSeite().getHelpKeys());
                parent.get(lang).getSubpages().add(p);
                pages.put(lang, p);

                // copy .html files
                File src = new File(seite.filenameHtml(lang));
                if (src.isFile()) {
                    FileService.copyFile(src, new File(targetFolder, lang));
                }
            }
            // copy images
            File src = new File(sourceImgFolder, seite.getId());
            if (src.isDirectory()) {
                FileService.copyFiles(src, new File(targetImgFolder, seite.getId()));
            }

            copyHtmlAndImg(seite.getSeiten(), pages, targetFolder); // recursive
        }
    }
}
