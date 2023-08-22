package minerva.seite;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.config.MinervaFactory;
import minerva.persistence.gitlab.git.HCommit;

public class SeiteHistoryPage extends SPage {

    @Override
    protected void execute() {
        boolean followRenames = "fr".equals(ctx.queryParam("m"));
        MinervaFactory fac = MinervaWebapp.factory();
        fac.gitlabOnlyPage();

        List<HCommit> commits = fac.getBackendService().getSeiteMetaHistory(seite, followRenames);
        Set<String> authors = new TreeSet<>();

        header(seite.getTitle() + " - " + n("history"));
        DataList list = list("commits");
        for (HCommit commit : commits) {
            DataMap map = list.add();
            map.put("hash", esc(commit.getHash()));
            map.put("hash7", esc(commit.getHash7()));
            map.put("gitlabCommitLink", fac.getBackendService().getCommitLink(commit.getHash()));
            String author = fac.login2RealName(commit.getAuthor());
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
