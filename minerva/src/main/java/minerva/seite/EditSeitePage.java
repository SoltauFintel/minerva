package minerva.seite;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.model.SeiteSO;
import minerva.model.UserSO.LoginAndEndTime;
import minerva.postcontents.PostContentsService;
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
            put("postcontentslink", "/post-contents/seite?key=" + u(getKey()));
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
        ISeitePCD data = waitForContent(version);
        
        seiteSO.saveAll(data.getTitle(), data.getContent(), version, data.getComment(), langs, start);
        
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

    protected ISeitePCD waitForContent(int version) {
        return (ISeitePCD) new PostContentsService().waitForContents(getKey(), version);
    }
    
    private String getKey() {
        return seite.getId() + ":" + branch + ":" + bookFolder + ":seite";
    }
}
