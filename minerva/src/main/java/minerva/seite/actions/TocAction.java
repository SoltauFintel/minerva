package minerva.seite;

public class TocAction extends SAction {

    @Override
    protected void execute() {
        int tocHeadingsLevels = i("tocHeadingsLevels");
        int tocSubpagesLevels = i("tocSubpagesLevels");

        Seite s = seite.getSeite();
        if (s.getTocHeadingsLevels() != tocHeadingsLevels || s.getTocSubpagesLevels() != tocSubpagesLevels) { // is dirty?
            s.setTocHeadingsLevels(tocHeadingsLevels);
            s.setTocSubpagesLevels(tocSubpagesLevels);
            seite.saveMeta(seite.commitMessage("TOC"));
        }
        
        ctx.redirect(viewlink);
    }
    
    private int i(String label) {
    	return Integer.valueOf(ctx.queryParam(label).substring(0, 1));
    }
}
