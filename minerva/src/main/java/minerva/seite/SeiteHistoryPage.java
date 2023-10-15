package minerva.seite;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.config.ICommit;
import minerva.config.MinervaFactory;
import minerva.user.UserAccess;

public class SeiteHistoryPage extends SPage {

    @Override
    protected void execute() {
        boolean followRenames = "fr".equals(ctx.queryParam("m"));
        MinervaFactory fac = MinervaWebapp.factory();
        fac.gitlabOnlyPage();

        List<ICommit> commits = fac.getBackendService().getSeiteMetaHistory(seite, followRenames);
        Set<String> authors = new TreeSet<>();

        header(seite.getTitle() + " - " + n("history"));
        DataList list = list("commits");
        for (ICommit commit : commits) {
            DataMap map = list.add();
            map.put("hash", esc(commit.getHash()));
            map.put("hash7", esc(commit.getHash7()));
            map.put("gitlabCommitLink", fac.getBackendService().getCommitLink(commit.getHash()));
            String author = UserAccess.login2RealName(commit.getAuthor());
            authors.add(author);
            map.put("author", esc(author));
            map.put("date", commit.getCommitDateTime());
            map.put("message", esc(commit.getMessage()));
        }
        put("authors", esc(authors.stream().collect(Collectors.joining(", "))));
        putInt("n", commits.size());
        put("filename", esc(seite.gitFilenameMeta()));
        put("followRenames", followRenames);
    }
}
