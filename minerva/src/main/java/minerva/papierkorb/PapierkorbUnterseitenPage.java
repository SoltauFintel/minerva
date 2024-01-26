package minerva.papierkorb;

import minerva.model.PapierkorbSO;
import minerva.workspace.WPage;

/**
 * Alle Unterseiten zu einem Papierkorb-Eintrag anzeigen
 */
public class PapierkorbUnterseitenPage extends WPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        PapierkorbSO papierkorb = workspace.getPapierkorb();
        WeggeworfeneSeite ws = papierkorb.byId(id);
        
        header(n("subpages"));
        String title = ws.getTitle().getString(workspace.getUser().getPageLanguage());
        String recycleSubpagesInfo = n(papierkorb.countSubpages(ws) == 1 ? "recycleSubpagesInfo1" : "recycleSubpagesInfo");
        recycleSubpagesInfo = recycleSubpagesInfo.replace("$t", title);
        put("recycleSubpagesInfo", esc(recycleSubpagesInfo));
        StringBuilder sb = new StringBuilder();
        ws.getUnterseiten().forEach(sub -> addSeiten(sub, sb));
        put("subpages", sb.toString());
    }
    
    private void addSeiten(WSeite ws, StringBuilder sb) {
        sb.append("<li>" + esc(ws.getTitle().getString(workspace.getUser().getPageLanguage())));
        if (!ws.getUnterseiten().isEmpty()) {
            sb.append("<ul>");
            for (WSeite sub : ws.getUnterseiten()) {
                addSeiten(sub, sb); // recursive
            }
            sb.append("</ul>");
        }
        sb.append("</li>\n");
    }
}
