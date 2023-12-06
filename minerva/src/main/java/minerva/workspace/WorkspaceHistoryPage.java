package minerva.workspace;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;
import com.github.template72.data.IDataMap;

import minerva.MinervaWebapp;
import minerva.base.Uptodatecheck;
import minerva.config.BackendService;
import minerva.config.ICommit;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.user.UserAccess;

public class WorkspaceHistoryPage extends WPage implements Uptodatecheck {

    @Override
    protected void execute() {
        int start = qp("start", 0, 0, Integer.MAX_VALUE);
        int size = qp("size", 200, 1, 500);
        
        BackendService bs = MinervaWebapp.factory().getBackendService();
        List<ICommit> commits = bs.getHtmlChangesHistory(workspace, start, size);
        String url = bs.getCommitLink("{t}");
        
        DataList changes = list("changes");
        DataMap c = null;
        DataList list = null;
        String prevUser = "{";
        boolean empty = true;
        for (ICommit commit : commits) {
            for (String dn : commit.getFiles()) { // dn is e.g. "handbuch/en/alw123.html"
                CommitEntry ce = new CommitEntry(dn, branch, workspace);
                String author = getAuthor(commit);
                if (ce.seite == null || exists(ce.link, list)) {
                    continue;
                } else if (!author.equals(prevUser)) {
                    c = changes.add(); // Gruppenwechsel
                    c.put("user", esc(UserAccess.login2RealName(author)));
                    list = c.list("pages");
                }
                prevUser = author;
                add(commit, ce.seite, ce.link, url, list.add());
                empty = false;
            }
        }
        putInt("nextStart", start += size);
        put("empty", empty);
        header(n("workspaceHistory").replace("$b", branch));
    }

    private String getAuthor(ICommit commit) {
        String author = commit.getAuthor();
        if (author.contains(" ")) { // "user2 user2" -> "user2"
            String[] w = author.split(" ");
            if (w.length == 2 && w[0].equals(w[1])) {
                author = w[0];
            }
        }
        return author;
    }
    
    private int qp(String key, int pDefault, int min, int max) {
        int value;
        try {
            value = Integer.parseInt(ctx.queryParam(key));
        } catch (NumberFormatException e) {
            value = pDefault;
        }
        if (value < min) {
            value = min;
        }
        if (value > max) {
            value = max;
        }
        return value;
    }

    private static class CommitEntry {
        SeiteSO seite;
        String link;
        
        private CommitEntry(String dn, String branch, WorkspaceSO workspace) {
            int o  = dn.indexOf("/");
            if (o < 0) {
                return;
            }
            int oo = dn.lastIndexOf("/");
            String bookFolder = dn.substring(0, o);
            String dnt = dn.substring(oo + 1);
            String id = dnt.substring(0, dnt.length() - ".html".length());
            try {
                BookSO book = workspace.getBooks().byFolder(bookFolder);
                seite = book._seiteById(id);
                link = "/s/" + branch + "/" + bookFolder + "/" + seite.getId();
            } catch (Exception fallThru) { // book or page does not exist
            }
        }
    }

    private boolean exists(String link, DataList list) {
        if (list != null) {
            for (IDataMap entry : list) {
                if (entry.get("link").toString().equals(link)) {
                    return true; // continue
                }
            }
        }
        return false;
    }

    private void add(ICommit commit, SeiteSO seite, String link, String url, DataMap map) {
        map.put("commitlink", esc(url.replace("{t}", commit.getHash())));
        String title = seite.getTitle();
        String m = commit.getMessage();
        String t = title + ": ";
        if (m.startsWith(t)) {
            m = m.substring(t.length());
        }
        if (m.equals(title)) {
            m = "";
        }
        map.put("commitMessage", esc(m));
        map.putHas("commitMessage", esc(m));
        map.put("pageTitle", esc(title));
        map.put("date", esc(commit.getCommitDateTime()));
        map.put("link", link);
    }
}
