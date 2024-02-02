package minerva.seite;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.model.SeiteSO;
import minerva.model.UserSO.LoginAndEndTime;
import minerva.seite.link.InvalidLinksModel;

public class EditSeitePage extends ViewSeitePage {

    @Override
    protected void execute2(String branch, String bookFolder, String id, SeiteSO seiteSO) {
        if (isPOST()) {
            Logger.info(user.getLogin() + " | " + branch + " | saving page #" + id + " \"" + seiteSO.getTitle() + "\" ..." + saveinfo());
            save(branch, bookFolder, id, seiteSO);
            workspace.onEditing(seite, true); // editing finished
        } else { // edit
            if (!"cl".equals(ctx.queryParam("m"))) {
                LoginAndEndTime lockedBy = user.hasEditingStarted(workspace.getBranch(), id);
                if (lockedBy != null) {
                    render = false;
                    ctx.redirect(viewlink + "/locked");
                    return;
                }
            }
            MinervaWebapp.factory().getBackendService().uptodatecheck(workspace, () -> seiteSO.freshcheck(langs));
            workspace.onEditing(seite, false); // editing started
            
            super.execute2(branch, bookFolder, id, seiteSO);
        }
    }
    
    protected String saveinfo() {
        return "";
    }
    
    @Override
    protected String modifyHeader(String header) {
        return "edit: " + header;
    }

    private void save(String branch, String bookFolder, String id, SeiteSO seiteSO) {
        long start = System.currentTimeMillis();
        int version = Integer.parseInt(ctx.formParam("version"));
        IPostContentsData data = waitForContent(branch, bookFolder, id, version);
        
        seiteSO.saveAll(data.getTitle(), data.getContent(), version, data.getComment(), langs, start);
        data.setDone(true);
        
        user.setLastEditedPage(seite.getId());

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

    protected IPostContentsData waitForContent(String branch, String bookFolder, String id, int version) {
        PostContentsData data = null;
        long max = 1000 * 60 * 2;
        long start = System.currentTimeMillis();
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
