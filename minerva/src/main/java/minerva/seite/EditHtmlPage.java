package minerva.seite;

import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.model.SeiteSO;

public class EditHtmlPage extends EditSeitePage {

    @Override
    protected void execute2(String branch, String bookFolder, String id, SeiteSO seiteSO) {
        user.onlyAdmin();
        super.execute2(branch, bookFolder, id, seiteSO);
        put("editlink", "/s/" + branch + "/" + bookFolder + "/" + id + "/html");
    }
    
    @Override
    protected String saveinfo() {
        return " (HTML editing mode)"; // give hint of HTML editing in log message
    }
    
    @Override
    protected String modifyHeader(String header) {
        return "edit HTML: " + header; // give hint of HTML editing in webpage title
    }
    
    @Override
    protected String getComment() {
        String comment = super.getComment();
        // give hint of HTML editing in commit message
        if (!comment.toUpperCase().contains("HTML")) {
            if (comment.isEmpty()) {
                comment = "(HTML)";
            } else {
                comment += " (HTML)";
            }
        }
        return comment;
    }
    
    @Override
    protected IPostContentsData waitForContent(String branch, String bookFolder, String id, int version) {
        IPostContentsData ret = new IPostContentsData() {
            @Override
            public NlsString getContent() {
                NlsString ret = new NlsString();
                for (String lang : langs) {
                    String html = ctx.formParam("htmlEditor" + lang.toUpperCase());
                    html = StringService.prettyHTML(html);
                    ret.setString(lang, html);
                }
                return ret;
            }

            @Override
            public void setDone(boolean done) { //
            }
        };
        return ret;
    }
}
