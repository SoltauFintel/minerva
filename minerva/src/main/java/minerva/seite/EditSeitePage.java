package minerva.seite;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.base.MinervaPageInitializer;
import minerva.base.NlsString;
import minerva.model.SeiteSO;
import minerva.model.UserSO.LoginAndEndTime;
import minerva.postcontents.PostContentsService;
import minerva.seite.link.InvalidLinksModel;
import minerva.seite.link.Link;
import minerva.seite.link.LinkService;
import ohhtml.toc.TocMacro;

public class EditSeitePage extends ViewSeitePage {

    @Override
    protected void execute2() {
        user.getJournal().clearLivesave(branch, id);
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
            calculateEditorHeight();
            put("postcontentslink", "/post-contents/seite?key=" + u(getKey()));
            put("livesavelink", "/s/" + branch + "/" + bookFolder + "/" + id + "/live-save");
            put("leftAreaContent", "");
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
        NlsString content = data.getContent();
        changeMinervaLink(content);
        seiteSO.saveAll(data.getTitle(), content, version, data.getComment(), langs, start, null);
        
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
    
    private void changeMinervaLink(NlsString content) {
        for (String lang : langs) {
            String html = content.getString(lang);
            if (html != null) {
                List<Link> links = LinkService.extractLinks(html, false);
                for (Link link : links) {
                    if (link.getHref().startsWith("http://minerva:9000/s/")) {
                        user.log("Illegal absolute link on page " + id + "/" + lang + ": " + link.getHref());
                    }
                }
            }
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

    private void calculateEditorHeight() {
        int vh = 330;
        if (user.getUser().isShowQuickbuttons()) {
            vh += 27;
        }
        if (!MinervaPageInitializer.isMasterBranch(ctx) || user.getCustomerMode().isActive()) {
            vh += 25;
        }
        putInt("vh", vh);
    }

    @Override
    protected String getEditLink(boolean isReleaseNotes) {
        return super.getEditLink(false);
    }
}
