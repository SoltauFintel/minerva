package minerva.subscription;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.rest.RestResponse;
import minerva.MinervaWebapp;
import minerva.base.FileService;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.model.SeiteSO;
import minerva.model.StateSO;
import minerva.model.WorkspaceSO;
import minerva.seite.Seite;
import minerva.user.User;

public class SubscriptionService {
    private static final String LOGIN = "SubscriptionService";

    public void subscribe(String url) {
        checkIfValid(url);
        new Thread(() -> pushData(url)).start();
    }
    
    private void checkIfValid(String url) {
        String valid = System.getenv("SUBSCRIBERS");
        if (!StringService.isNullOrEmpty(valid)) {
            String[] w = valid.split(",");
            for (String i : w) {
                if (i.trim().equals(url)) {
                    return; // is valid
                }
            }
        }
        Logger.error("Unknown subscriber: " + url + "\nSUBSCRIBERS: " + valid);
        throw new RuntimeException("Unknown subscriber");
    }
    
    private void pushData(String url) {
        // login
        if (!MinervaWebapp.factory().isCustomerVersion() || MinervaWebapp.factory().isGitlab()) {
            throw new RuntimeException("pushData() can only be called for customer version with file-system backend!");
        }
        String folder = System.getenv("MINERVA_USERFOLDER");
        if (StringService.isNullOrEmpty(folder)) {
            folder = LOGIN;
        }
        Logger.info("pushData() | folder: " + folder + " | subscriber: " + url);
        StateSO state = new StateSO(new User(LOGIN, folder));
        
        // get book
        WorkspaceSO workspace = state.getUser().getWorkspaces().master();
        int n = workspace.getBooks().size();
        if (n != 1) {
            throw new RuntimeException("1 book is expected but there are " + n + " book(s).");
        }
        
        // zip it
        File zipFile = zip(new File(workspace.getFolder()));
        try {
            // upload it to url
            upload(zipFile, url + "/upload");
            Logger.info("pushData() | Upload of " + zipFile.getAbsolutePath() + " to " + url + "/upload completed.");
        } finally {
            zipFile.delete();
        }
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

    private void upload(File zipFile, String url) {
        new REST(url) {
            @Override
            protected RestResponse request(HttpEntityEnclosingRequestBase request, String str, String contentType) {
                request.setEntity(new FileEntity(zipFile, ContentType.APPLICATION_OCTET_STREAM)); // TODO Amalia
                return doRequest(request);
            }
        }.post("").close();
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
        String subscribers = System.getenv("SUBSCRIBERS");
        if (!StringService.isNullOrEmpty(subscribers)) {
            String[] w = subscribers.split(",");
            for (String host : w) {
                new REST(host + "/book6/page/" + page.getId()).put(page).close();
            }
        }
    }
    
    public void pageDeleted(String id) {
        String subscribers = System.getenv("SUBSCRIBERS");
        if (!StringService.isNullOrEmpty(subscribers)) {
            String[] w = subscribers.split(",");
            for (String host : w) {
                new REST(host + "/book6/page/" + id).delete().close(); // TODO Amalia: DELETE with body
            }
        }
    }
}
