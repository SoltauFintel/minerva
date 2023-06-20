package minerva.seite;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.git.HCommit;

public class SeiteHistoryPage extends SPage {

    @Override
    protected void execute() {
        if (!MinervaWebapp.factory().isGitlab()) {
            throw new RuntimeException("Page only for Gitlab mode");
        }

        List<HCommit> commits = MinervaWebapp.factory().getGitlabRepository().getSeiteMetaHistory(seite);

        header(seite.getTitle() + " - " + n("history"));
        DataList list = list("commits");
        for (HCommit commit : commits) {
            DataMap map = list.add();
            map.put("hash", esc(commit.getHash()));
            map.put("hash7", esc(commit.getHash7()));
            map.put("author", esc(commit.getAuthor()));
            map.put("date", commit.getCommitDateTime());
            map.put("message", esc(commit.getMessage()));
        }
    }
}
