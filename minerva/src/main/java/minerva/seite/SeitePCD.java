package minerva.seite;

import github.soltaufintel.amalia.spark.Context;
import minerva.MinervaWebapp;
import minerva.base.NlsString;
import minerva.postcontents.PostContentsData;

public class SeitePCD extends PostContentsData implements ISeitePCD {
    private final NlsString content = new NlsString();
    private final NlsString title = new NlsString();
    private final String comment;
    
    public SeitePCD(Context ctx) {
        super(ctx.queryParam("key"), Integer.parseInt(ctx.formParam("version")));
        comment = ctx.formParam("comment");
        for (String lang : MinervaWebapp.factory().getLanguages()) {
            String LANG = lang.toUpperCase();
            content.setString(lang, ctx.formParam("content" + LANG));
            title.setString(lang, ctx.formParam("titel" + LANG));
        }
    }

    @Override
    public NlsString getContent() {
        return content;
    }

    @Override
    public NlsString getTitle() {
        return title;
    }

    @Override
    public String getComment() {
        return comment;
    }
}
