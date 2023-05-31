package minerva.seite;

import minerva.user.UAction;

public class PostContentsAction extends UAction {
    private static final String handle = "handle";
    public static PostContentsData last;
    
    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String id = ctx.pathParam("id");
        int version = Integer.parseInt(ctx.formParam("version"));

        PostContentsData d = new PostContentsData(branch, bookFolder, id, version);
        for (String lang : langs) {
            d.getContent().setString(lang, ctx.formParam("content" + lang.toUpperCase()));
        }
        synchronized (handle) {
            d.setParent(last);
            last = d;
        }
    }
    
    public static PostContentsData get(String branch, String bookFolder, String id, int version) {
        synchronized (handle) {
            PostContentsData pick = last;
            while (pick != null) {
                if (pick.getBranch().equals(branch) && pick.getBookFolder().equals(bookFolder)
                        && pick.getId().equals(id) && pick.getVersion() == version) {
                    return pick;
                }
                pick = pick.getParent();
            }
        }
        return null;
    }
}
