package minerva.seite;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;
import ohhtml.toc.HelpKeysForHeading;

public class AllHelpKeysPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        WorkspaceSO workspace = user.getWorkspace(branch);
        DataList list = list("helpKeys");
        for (BookSO book : workspace.getBooks()) {
            fill(book, branch, list);
        }
        list.sort((a, b) -> a.getValue("helpKey").toString().compareToIgnoreCase(b.getValue("helpKey").toString()));
        putSize("n", list);
        header(n("allHelpKeys"));
    }
    
    private void fill(BookSO book, String branch, DataList list) {
        String linkPrefix = "/s/" + esc(branch) + "/" + esc(book.getBook().getFolder()) + "/";
        for (SeiteSO seite : book.getAlleSeiten()) {
            for (String helpKey : seite.getSeite().getHelpKeys()) {
                DataMap map = list.add();
                map.put("helpKey", esc(helpKey));
                map.put("link", linkPrefix + seite.getId());
                map.put("pageTitle", esc(seite.getTitle()));
            }
            if (seite.getSeite().getHkh() != null) {
            	for (HelpKeysForHeading hkh : seite.getSeite().getHkh()) {
            		for (String helpKey : hkh.getHelpKeys()) {
            			DataMap map = list.add();
            			map.put("helpKey", esc(helpKey));
            			map.put("link", linkPrefix + seite.getId());
            			map.put("pageTitle", esc(seite.getTitle() + " > " + hkh.getHeading()));
            		}
				}
            }
        }
    }
}
