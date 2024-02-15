package minerva.seite.tag;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.workspace.WPage;

public class TagCloudPage extends WPage {

    @Override
    protected void execute() {
        List<TagN> tags = workspace.getAllTags().sortedByTag();

        header(n("tagCloud"));
        DataList list = list("tags");
        for (TagN tag : tags) {
            DataMap map = list.add();
            map.put("tag", esc(tag.getTag()));
            map.putInt("n", tag.getAnzahl());
            map.put("link", "/w/" + branch + "/tag/" + esc(tag.getTag()));
            map.putInt("size", size(tag));
        }
    }

	private int size(TagN tag) {
		int size;
		final int n = tag.getAnzahl();
		if (n >= 70) {
			size = 35;
		} else if (n >= 30) {
			size = 27;
		} else if (n >= 10) {
			size = 22;
		} else if (n >= 3) {
			size = 16;
		} else {
			size = 11;
		}
		return size;
	}
}
