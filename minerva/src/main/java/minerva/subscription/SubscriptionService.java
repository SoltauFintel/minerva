package minerva.subscription;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import gitper.base.FileService;
import minerva.MinervaWebapp;
import minerva.base.NlsString;
import minerva.model.SeiteSO;
import minerva.model.StateSO;
import minerva.model.WorkspaceSO;
import minerva.seite.Seite;
import minerva.user.User;

public class SubscriptionService {
    private static final String LOGIN = "SubscriptionService";
    private final SubscribersAccess subscribersAccess = new SubscribersAccess();
    
    public void subscribe(String url) {
        subscribersAccess.checkIfValid(url);
        uploadZip(zipFile -> subscribersAccess.uploadZip(zipFile, url)); // pagesChanged for 1 subscriber
    }
    
    private File createZipFile() {
        // login
        User user = new User();
        user.setLogin(LOGIN);
        StateSO state = new StateSO(user);
        
        // get book
        WorkspaceSO workspace = state.getUser().masterWorkspace();
        int n = workspace.getBooks().size();
        if (n != 1) {
            throw new RuntimeException("1 book is expected but there are " + n + " books.");
        }
        
        // zip it
        return zip(new File(workspace.getFolder()));
    }
    
    private File zip(File folder) {
        try {
            File zipFile = Files.createTempFile("minerva-push-", ".zip").toFile();
            FileService.zip(folder, zipFile);
            return zipFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TPage createTPage(SeiteSO seite, NlsString content, List<String> langs) {
        TPage p = new TPage();
        Seite s = seite.getSeite();
        p.setId(s.getId());
        p.setParentId(s.getParentId());
        p.getTitle().from(s.getTitle());
        p.setPosition(s.getPosition());
        p.setSorted(s.isSorted());
        p.getTags().addAll(s.getTags());
        p.getHelpKeys().addAll(s.getHelpKeys());
        p.setHtml(new HashMap<>());
        for (String lang : langs) {
            p.getHtml().put(lang, content.getString(lang));
        }
        return p;
    }
    
    public void pageModified(TPage page) {
        if (subscribersAccess.hasSubscribers()) {
            checkMode();
            new Thread(() -> subscribersAccess.put(page)).start();
        }
    }
    
    /**
     * Page has been deleted or made invisible.
     * @param id page ID
     */
    public void pageDeleted(String id) {
        if (subscribersAccess.hasSubscribers()) {
            checkMode();
            new Thread(() -> subscribersAccess.delete(id)).start();
        }
    }

    /**
     * Page moved, pages reordered, sorting activated, page made visible.
     * Those changes are so massive that all data will be pushed to all subscribers.
     */
    public void pagesChanged() {
        // ** pagesChanged for all subscribers **
        uploadZip(zipFile -> subscribersAccess.uploadZip(zipFile));
    }

    private void uploadZip(Consumer<File> action) {
        if (subscribersAccess.hasSubscribers()) {
            checkMode();
            new Thread(() -> {
                File zipFile = createZipFile();
                try {
                    action.accept(zipFile);
                } finally {
                    zipFile.delete();
                }
            }).start();
        }
    }
    
    private void checkMode() {
        if (!MinervaWebapp.factory().isCustomerVersion() || MinervaWebapp.factory().isGitlab()) {
            throw new RuntimeException("Method can only be called for customer version with file-system backend!");
        }
    }
    
    public PageTitles loadPageTitles() {
        /*if (true) { // dev mode
            PageTitles ret = new PageTitles();
            ret.setLang(new HashMap<>());
            List<PageTitle> z = new ArrayList<>();
            PageTitle t;
            t = new PageTitle();
            t.setId("10");
            t.setTitle("Hallo Seite 1");
            z.add(t);
            t = new PageTitle();
            t.setId("12");
            t.setTitle("Hallo Seite 2");
            z.add(t);
            ret.getLang().put("de", z);
            t = new PageTitle();
            t.setId("14");
            t.setTitle("Hallo Seite 4");
            z.add(t);
            ret.getLang().put("de", z);
            return ret;
        }*/
        if (!subscribersAccess.hasSubscribers()) {
            return new PageTitles();
        }
        checkMode();
        List<PageTitles> pageTitlesList = subscribersAccess.loadPageTitles();
        PageTitles ret = null;
        for (PageTitles titles : pageTitlesList) {
            if (ret == null) {
                ret = titles;
            } else {
                for (String lang : titles.getLang().keySet()) {
                    List<PageTitle> source = titles.getLang().get(lang);
                    List<PageTitle> target = ret.getLang().get(lang);
                    if (target == null) {
                        ret.getLang().put(lang, source);
                    } else {
                        for (PageTitle t : source) {
                            if (!find(t, target)) {
                                target.add(t);
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    private boolean find(PageTitle t, List<PageTitle> list) {
        for (PageTitle i : list) {
            if (i.getId().equals(t.getId()) && i.getTitle().equals(i.getTitle())) {
                // can result in same ID but different titles
                return true;
            }
        }
        return false;
    }
}
