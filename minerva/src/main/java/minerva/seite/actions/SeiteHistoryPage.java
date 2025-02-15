package minerva.seite.actions;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import gitper.BackendService;
import gitper.base.ICommit;
import minerva.MinervaWebapp;
import minerva.book.BookPage;
import minerva.config.MinervaFactory;
import minerva.model.SeiteSO;
import minerva.seite.SPage;
import minerva.user.UserAccess;

public class SeiteHistoryPage extends SPage {

    @Override
    protected void execute() {
        boolean followRenames = "fr".equals(ctx.queryParam("m"));

        MinervaFactory fac = MinervaWebapp.factory();
        fac.gitlabOnlyPage();
        BackendService backend = fac.getBackendService();
        String filenameMeta = seite.getBook().getBook().getFolder() + "/" + seite.getId() + SeiteSO.META_SUFFIX;
        List<ICommit> commits = backend.getFileHistory(filenameMeta, seite.getBook().getWorkspace(), followRenames);

        header(seite.getTitle() + " - " + n("history"));
        putCommits(commits, backend, model);
        put("filename", esc(seite.gitFilenameMeta()));
        put("followRenames", followRenames);

        if (book.isNotPublic()) {
            BookPage.oneLang(model, book);
        }
    }
    
    public static void putCommits(List<ICommit> commits, BackendService backend, DataMap model) {
        Set<String> authors = new TreeSet<>();
        DataList list = model.list("commits");
        for (ICommit commit : commits) {
            DataMap map = list.add();
            putCommit(commit, backend, authors, map);
        }
        model.put("authors", Escaper.esc(authors.stream().collect(Collectors.joining(", "))));
        model.putSize("n", commits);
    }

    public static void putCommit(ICommit commit, BackendService backend, Set<String> authors, DataMap map) {
        map.put("hash", Escaper.esc(commit.getHash()));
        map.put("hash7", Escaper.esc(commit.getHash7()));
        map.put("gitlabCommitLink", backend.getCommitLink(commit.getHash()));
        String author = UserAccess.login2RealName(commit.getAuthor());
        if (authors != null) {
            authors.add(author);
        }
        map.put("author", Escaper.esc(author));
        map.put("date", Escaper.esc(commit.getCommitDateTime()));
        map.put("message", Escaper.esc(commit.getMessage()));
    }
}
