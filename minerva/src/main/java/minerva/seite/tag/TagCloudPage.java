package minerva.seite.tag;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.user.UPage;

public class TagCloudPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        boolean sortedByN = "n".equals(ctx.queryParam("m"));

        TagNList allTags = user.getWorkspace(branch).getAllTags();
        List<TagN> tags = sortedByN ? allTags.sortedByN() : allTags.sortedByTag();

        DataList list = list("tags");
        for (TagN tag : tags) {
            DataMap map2 = list.add();
            map2.put("tag", esc(tag.getTag()));
            map2.putInt("n", tag.getAnzahl());
            map2.put("link", "/w/" + branch + "/tag/" + esc(tag.getTag()));
        }
        header(n("tagCloud"));
    }
}
