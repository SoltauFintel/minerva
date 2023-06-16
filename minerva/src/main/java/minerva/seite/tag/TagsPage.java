package minerva.seite.tag;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.seite.SPage;

public class TagsPage extends SPage {

    @Override
    protected void execute() {
        String me = viewlink + "/tags";
        if (isPOST()) { // create tag
            String tag = ctx.formParam("tag");
            seite.tags().addTag(tag);
            ctx.redirect(me);
        } else { // list tags
            header("tags");
            put("addlink", me);
            DataList list = list("tags");
            seite.getSeite().getTags().stream().sorted().forEach(tag -> {
                DataMap map = list.add();
                map.put("tag", esc(tag));
                map.put("deletelink", viewlink + "/delete-tag?tag=" + Escaper.urlEncode(tag, ""));
            });
            DataList list2 = list("suggestedTags");
            for (String tag : book.getWorkspace().getExclusions().getSuggestedTags(seite.getSeite().getTags())) {
                list2.add().put("tag", esc(tag));
            }
        }
    }
}
