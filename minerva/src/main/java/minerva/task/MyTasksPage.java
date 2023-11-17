package minerva.task;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.StringService;
import minerva.base.Uptodatecheck;
import minerva.seite.Note;
import minerva.seite.note.NoteWithSeite;
import minerva.user.UserAccess;
import minerva.workspace.WPage;

public class MyTasksPage extends WPage implements Uptodatecheck {

    @Override
    protected void execute() {
    	String login = ctx.queryParam("login");
		Logger.info(user.getLogin() + " | " + (login == null || user.getLogin().equals(login) ? "My tasks" : "All tasks for " + login));
		put("login", esc(UserAccess.login2RealName(StringService.isNullOrEmpty(login) ? user.getLogin() : login)));
        header(n("myTasks"));
        List<NoteWithSeite> notes = user.getNotes(branch, login);
		fill(notes, branch, model, user.getLogin());
		putInt("n", notes.size());
    }
    
    private void fill(List<NoteWithSeite> notes, String branch, DataMap model, String login) {
        String v0 = "/s/" + branch + "/";
        notes.sort((a, b) -> b.getNote().getCreated().compareTo(a.getNote().getCreated()));
        DataList list = model.list("notes");
        for (NoteWithSeite n : notes) {
            DataMap map = list.add();
            Note note = n.getNote();
            map.put("id", n.getSeite().getId() + "-" + note.getId());
            map.put("me", note.getUser().equals(login));
            map.put("user", esc(UserAccess.login2RealName(note.getUser())));
            map.put("date", esc(note.getCreated()));
            String text = note.getText();
            if (text.length() > 113) {
                map.put("text1", StringService.makeClickableLinks(esc(text.substring(0, 110))));
                map.put("hasMoreText", true);
            } else {
                map.put("text1", StringService.makeClickableLinks(esc(text)));
                map.put("hasMoreText", false);
            }
            map.put("completeText", StringService.makeClickableLinks(esc(text)));
            String bookFolder = n.getSeite().getBook().getBook().getFolder();
            String v = v0 + bookFolder + "/" + n.getSeite().getId();
            map.put("link", v + "/notes?highlight=" + note.getId() + "#" + note.getId());
            map.put("pagelink", v);
            map.put("pageTitle", esc(n.getSeite().getTitle()));
        }
        model.putInt("n", notes.size());
    }
}
