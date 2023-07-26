package minerva.workspace;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;
import com.github.template72.data.IDataMap;

import minerva.MinervaWebapp;
import minerva.base.Uptodatecheck;
import minerva.model.BookSO;
import minerva.model.GitlabRepositorySO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.persistence.gitlab.git.HCommit;

public class WorkspaceHistoryPage extends WPage implements Uptodatecheck {

    @Override
    protected void execute() {
        int start = qp("start", 0, 0, Integer.MAX_VALUE);
        int size = qp("size", 200, 1, 500);
        
        GitlabRepositorySO repo = MinervaWebapp.factory().getGitlabRepository();
        List<HCommit> commits = repo.getHtmlChangesHistory(workspace, start, size);
        String url = repo.getProjectUrl() + MinervaWebapp.factory().getConfig().getGitlabCommitPath(); // http://host:port/user/repo/-/commit/
        
        DataList changes = list("changes");
        DataMap c = null;
        DataList list = null;
        String prevUser = "{";
        boolean empty = true;
        for (HCommit commit : commits) {
            for (String dn : commit.getFiles()) { // dn is e.g. "handbuch/en/alw123.html"
                CommitEntry ce = new CommitEntry(dn, branch, workspace);
                if (ce.seite == null || exists(ce.link, list)) {
                    continue;
                } else if (!commit.getAuthor().equals(prevUser)) {
                    c = changes.add(); // Gruppenwechsel
                    c.put("user", esc(commit.getAuthor()));
                    list = c.list("pages");
                }
                prevUser = commit.getAuthor();
                add(commit, ce.seite, ce.link, url, list.add());
                empty = false;
            }
        }
        putInt("nextStart", start += size);
        put("empty", empty);
        header(n("workspaceHistory").replace("$b", branch));
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
                seite = book.getSeiten()._byId(id);
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

    private void add(HCommit commit, SeiteSO seite, String link, String url, DataMap map) {
        map.put("commitlink", esc(url + commit.getHash()));
        map.put("pageTitle", esc(seite.getTitle()));
        map.put("date", esc(commit.getCommitDateTime()));
        map.put("link", link);
    }
}
