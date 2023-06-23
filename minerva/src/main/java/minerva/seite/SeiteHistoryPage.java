package minerva.seite;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.git.HCommit;
import minerva.model.GitlabRepositorySO;

public class SeiteHistoryPage extends SPage {

    @Override
    protected void execute() {
        if (!MinervaWebapp.factory().isGitlab()) {
            throw new RuntimeException("Page only for Gitlab mode");
        }

        GitlabRepositorySO repo = MinervaWebapp.factory().getGitlabRepository();
        String url = repo.getProjectUrl() + MinervaWebapp.factory().getConfig().getGitlabCommitPath(); // http://host:port/user/repo/-/commit/
        List<HCommit> commits = repo.getSeiteMetaHistory(seite);
        Set<String> authors = new TreeSet<>();

        header(seite.getTitle() + " - " + n("history"));
        DataList list = list("commits");
        for (HCommit commit : commits) {
            DataMap map = list.add();
            map.put("hash", esc(commit.getHash()));
            map.put("hash7", esc(commit.getHash7()));
            map.put("gitlabCommitLink", url + commit.getHash());
            map.put("author", esc(commit.getAuthor()));
            map.put("date", commit.getCommitDateTime());
            map.put("message", esc(commit.getMessage()));
            authors.add(commit.getAuthor());
        }
        put("authors", esc(authors.stream().collect(Collectors.joining(", "))));
        putInt("n", commits.size());
        put("filename", esc(seite.gitFilenameMeta()));
    }
}
