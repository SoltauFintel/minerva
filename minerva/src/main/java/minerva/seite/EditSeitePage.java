package minerva.seite;

import org.pmw.tinylog.Logger;

import minerva.base.NlsString;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.persistence.gitlab.UpToDateCheckService;
import minerva.seite.link.InvalidLinksModel;

public class EditSeitePage extends ViewSeitePage {

    @Override
    protected void execute2(String branch, String bookFolder, String id, SeiteSO seiteSO) {
        if (isPOST()) {
            Logger.info(user.getUser().getLogin() + " | " + branch + " | saving page #" + id + " ...");
            save(branch, bookFolder, id, seiteSO);
        } else { // edit
            WorkspaceSO workspace = seiteSO.getBook().getWorkspace();
            UpToDateCheckService.check(workspace, () -> seiteSO.freshcheck(langs));
            
            super.execute2(branch, bookFolder, id, seiteSO);
        }
    }
    
    @Override
    protected String modifyHeader(String header) {
        return "edit: " + header;
    }

    private void save(String branch, String bookFolder, String id, SeiteSO seiteSO) {
        long start = System.currentTimeMillis();
        int version = Integer.parseInt(ctx.formParam("version"));
        String comment = ctx.formParam("comment");
        if (comment == null) {
            comment = "";
        } else {
            comment = comment.trim();
        }
        NlsString title = new NlsString();
        for (String lang : langs) {
            String LANG = lang.toUpperCase();
            title.setString(lang, ctx.formParam("titel" + LANG).trim());
        }
        
        PostContentsData data = waitForContent(branch, bookFolder, id, version);
        
        seiteSO.saveAll(title, data.getContent(), version, comment, langs, start);
        data.setDone(true);

        InvalidLinksModel linksModel = new InvalidLinksModel(seiteSO, langs);
        if (linksModel.hasLinks()) {
            // Es müssen noch Links aufgelöst werden.
            user.setLinksModel(linksModel);
            ctx.redirect("/links/" + branch + "/" + bookFolder + "/" + id + "?index=0");
        } else {
            // Keine Links vorhanden oder Links so in Ordnung.
            ctx.redirect("/s/" + branch + "/" + bookFolder + "/" + id);
        }
    }

    private PostContentsData waitForContent(String branch, String bookFolder, String id, int version) {
        PostContentsData data = null;
        long start = System.currentTimeMillis();
        long max = 1000 * 60 * 5;
        do {
            data = PostContentsService.get(branch, bookFolder, id, version);
            if (data != null) {
                return data;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupt while waiting for the save to complete. Page ID: " + id, e);
            }
        } while (System.currentTimeMillis() - start < max);
        throw new RuntimeException("Timeout while waiting for the save to complete."
                + " Please update workspace. Page ID: " + id);
    }
}
