package minerva.seite;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.base.MinervaPageInitializer;
import minerva.base.NlsString;
import minerva.model.SeiteSO;
import minerva.model.UserSO.LoginAndEndTime;
import minerva.postcontents.PostContentsService;
import minerva.seite.link.InvalidLinksModel;
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
        examineHtml(content);
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
    
    private void examineHtml(NlsString content) {
        for (String lang : langs) {
            String html = content.getString(lang);
            if (html != null) {
                Document doc = Jsoup.parse(html);
                // illegal absolute Minerva link
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String href = link.attr("href");
                    if (href.startsWith("http://minerva:9000/s/")) {
                        user.log("Illegal absolute link on page " + id + " [" + lang + "]: " + href);
                    }
                }
                // illegal <h1>
                if (!doc.select("h1").isEmpty()) {
                    user.log("Illegal <h1> elements in HTML of page " + id + " [" + lang + "]");
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
