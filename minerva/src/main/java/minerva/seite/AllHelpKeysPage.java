package minerva.seite;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class AllHelpKeysPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        WorkspaceSO workspace = user.getWorkspace(branch);
        DataList list = list("helpKeys");
        for (BookSO book : workspace.getBooks()) {
            fill(book.getSeiten(), branch, list);
        }
        list.sort((a, b) -> a.getValue("helpKey").toString().compareToIgnoreCase(b.getValue("helpKey").toString()));
        putInt("n", list.size());
        header(n("allHelpKeys"));
    }
    
    private void fill(SeitenSO seiten, String branch, DataList list) {
        if (seiten.isEmpty()) {
            return;
        }
        String linkPrefix = "/s/" + esc(branch) + "/" + esc(seiten.get(0).getBook().getBook().getFolder()) + "/";
        for (SeiteSO seite : seiten) {
            for (String helpKey : seite.getSeite().getHelpKeys()) {
                DataMap map = list.add();
                map.put("helpKey", esc(helpKey));
                map.put("link", linkPrefix + seite.getId());
                map.put("pageTitle", esc(seite.getTitle()));
            }
            fill(seite.getSeiten(), branch, list); // recursive
        }
    }
}
