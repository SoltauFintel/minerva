package minerva.seite;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.model.SeiteSO;
import minerva.model.UserSO.LoginAndEndTime;
import minerva.postcontents.PostContentsService;
import minerva.seite.link.InvalidLinksModel;
import ohhtml.toc.TocMacro;

public class EditSeitePage extends ViewSeitePage {

    @Override
    protected void execute2() {
        if (isPOST()) {
            Logger.debug(seite.getLogLine(null) + " | Saving page..." + saveinfo());
            save(branch, bookFolder, id, seite);
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
            MinervaWebapp.factory().getBackendService().uptodatecheck(workspace, () -> seite.freshcheck(langs));
            workspace.onEditing(seite, false); // editing started
            
            super.execute2();
            put("postcontentslink", "/post-contents/seite?key=" + u(getKey()));
            seite.imagesBeforeEdit();
            Logger.info(seite.getLogLine(null) + " | *** start editing");
        }
    }

    @Override
    protected String transformContent(TocMacro macro, String lang, DataMap map) {
        return seite.getContent().getString(lang);
    }

    private void save(String branch, String bookFolder, String id, SeiteSO seiteSO) {
        long start = System.currentTimeMillis();
        
        int version = Integer.parseInt(ctx.formParam("version"));
        ISeitePCD data = waitForContent(version);
        
        saveSeite(branch, bookFolder, id, seiteSO, start, version, data);
    }

    protected ISeitePCD waitForContent(int version) {
        return (ISeitePCD) new PostContentsService().waitForContents(getKey(), version);
    }
    
    private String getKey() {
        return seite.getId() + ":" + branch + ":" + bookFolder + ":seite";
    }

    private void saveSeite(String branch, String bookFolder, String id, SeiteSO seiteSO, long start, int version,
            ISeitePCD data) {
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
    
    protected String saveinfo() {
        return "";
    }
    
    @Override
    protected String modifyHeader(String header) {
        return "edit: " + header;
    }
    
    @Override
    protected void pagemode() {
        setCKEditorPageMode();
    }
}
