package minerva.task;

import minerva.base.StringService;
import minerva.comment.Comment;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.CommentWithSeite;

/**
 * Task adapter for Comment
 */
public class CommentTask implements Task {
    private final CommentWithSeite cws;
    private final Comment comment;
    private final String parentLink;
    private final String link;
    private final String text;
    
    public CommentTask(CommentWithSeite cws, String branch) {
        this.cws = cws;
        this.comment = cws.getComment();
        String zt = branch + "/" + cws.getSeite().getBook().getBook().getFolder() + "/" + cws.getSeite().getId();
        parentLink = "/s/" + zt;
        link = "/sc/" + zt + "/comments?highlight=" + comment.getId() + "#" + comment.getId();
        
        String html = cws.getComment().getText();
        SeiteSO seite = cws.getSeite();
        BookSO book = seite.getBook();
        String prefix = "/sc/" + book.getWorkspace().getBranch() + "/" + book.getBook().getFolder() + "/"
                + seite.getId() + "/";
        for (String src : StringService.findHtmlTags(html, "img", "src")) {
            html = html.replace("src=\"" + src + "\"", "src=\"" + prefix + src + "\"");
        }
        text = html;
    }
    
    @Override
    public String getId() {
        return cws.getSeite().getId() + "-" + comment.getId();
    }

    @Override
    public String getLogin() {
        return comment.getUser();
    }
    
    @Override
    public String getPerson() {
        return comment.getPerson() == null ? "" : comment.getPerson();
    }

    @Override
    public String getDateTime() {
        return comment.getCreated();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getParentLink() {
        return parentLink;
    }

    @Override
    public String getParentTitle() {
        return cws.getSeite().getTitle();
    }

    @Override
    public String getTypeName() {
        return "Comment"; // it's a RB key
    }

    @Override
    public String getColor() {
        return "rgb(0,101,255)";
    }
}
