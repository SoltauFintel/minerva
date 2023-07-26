package minerva.seite;

import minerva.access.CommitMessage;

public class TocAction extends SAction {

    @Override
    protected void execute() {
        int tocLevels = Integer.valueOf(ctx.queryParam("tocLevels").substring(0, 1));
        boolean tocWithSubpages = "on".equals(ctx.queryParam("tocWithSubpages"));        

        Seite s = seite.getSeite();
        if (s.getTocLevels() != tocLevels || s.isTocWithSubpages() != tocWithSubpages) { // is dirty?
            s.setTocLevels(tocLevels);
            s.setTocWithSubpages(tocWithSubpages);
            seite.saveMeta(new CommitMessage(seite, "TOC"));
        }
        
        ctx.redirect(viewlink);
    }
}
