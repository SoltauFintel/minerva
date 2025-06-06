package minerva.seite;

import com.github.template72.data.DataMap;

import minerva.base.NlsString;
import minerva.base.TextService;
import ohhtml.toc.TocMacro;

public class EditHtmlPage extends EditSeitePage {

    @Override
    protected void execute2() {
        user.onlyAdmin();
        super.execute2();
        put("editlink", "/s/" + branch + "/" + bookFolder + "/" + id + "/html");
    }
    
    @Override
    protected String transformContent(TocMacro macro, String lang, DataMap map) {
		return super.transformContent(macro, lang, map).replace("&", "&amp;");
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
    protected ISeitePCD waitForContent(int version) {
        return new ISeitePCD() {
            @Override
            public NlsString getContent() {
                NlsString ret = new NlsString();
                for (String lang : langs) {
                    String html = ctx.formParam("htmlEditor" + lang.toUpperCase());
                    html = TextService.prettyHTML(html);
                    ret.setString(lang, html);
                }
                return ret;
            }

            @Override
            public NlsString getTitle() {
                NlsString ret = new NlsString();
                for (String lang : langs) {
                    ret.setString(lang, ctx.formParam("titel" + lang.toUpperCase()));
                }
                return ret;
            }

            @Override
            public String getComment() {
                String comment = ctx.formParam("comment");
                comment = comment == null ? "" : comment.trim();
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
        };
    }
    
    @Override
    protected void pagemode() {
        setJQueryObenPageMode();
    }
}
