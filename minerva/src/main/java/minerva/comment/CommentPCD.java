package minerva.comment;

import github.soltaufintel.amalia.spark.Context;
import minerva.postcontents.PostContentsData;

public class CommentPCD extends PostContentsData {
    private final String text;
    private final String person;
    
    public CommentPCD(Context ctx) {
        super(ctx.queryParam("key"), Integer.parseInt(ctx.formParam("version")));
        text = ctx.formParam("content");
        person = ctx.formParam("person");
    }

    public String getText() {
        return text;
    }

    public String getPerson() {
        return person;
    }
}
