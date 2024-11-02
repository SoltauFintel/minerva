package minerva.seite;

public class TocAction extends SAction {

    @Override
    protected void execute() {
        int tocHeadingsLevels = Integer.valueOf(ctx.queryParam("tocHeadingsLevels").substring(0, 1));
        int tocSubpagesLevels = Integer.valueOf(ctx.queryParam("tocSubpagesLevels").substring(0, 1));

        Seite s = seite.getSeite();
        if (s.getTocHeadingsLevels() != tocHeadingsLevels || s.getTocSubpagesLevels() != tocSubpagesLevels) { // is dirty?
            s.setTocHeadingsLevels(tocHeadingsLevels);
            s.setTocSubpagesLevels(tocSubpagesLevels);
            seite.saveMeta(seite.commitMessage("TOC"));
        }
        
        ctx.redirect(viewlink);
    }
}
