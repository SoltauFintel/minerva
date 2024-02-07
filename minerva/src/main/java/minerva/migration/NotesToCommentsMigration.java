package minerva.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.base.StringService;
import minerva.comment.Comment;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.Note;
import minerva.user.UserAccess;
import minerva.workspace.WAction;

public class NotesToCommentsMigration extends WAction {
    private String result = "";
    private final Map<String, String> files = new HashMap<>();
    private final Set<String> deleteFiles = new HashSet<>();

    @Override
    protected void execute() {
        try {
            int n = 0;
            for (BookSO book : workspace.getBooks()) {
                for (SeiteSO seite : book.getAlleSeiten()) {
                    if (!seite.getSeite().getNotes().isEmpty()) {
                        if (++n % 50 == 0) {
                            Logger.info("migration... " + n);
                        }
                        seite.getSeite().getNotes().forEach(note -> migrateNote(seite, note));
                    }
                }
            }
            if (!files.isEmpty()) {
                Logger.info("Migration: saving files...");
                workspace.dao().saveFiles(files, new CommitMessage("Notes migration (create comments)"), workspace);
            }
            if (!deleteFiles.isEmpty()) {
                Logger.info("Migration: deleting old files...");
                List<String> cantBeDeleted = new ArrayList<>();
                workspace.dao().deleteFiles(deleteFiles, new CommitMessage("Notes migration (delete notes)"), workspace, cantBeDeleted);
                if (!cantBeDeleted.isEmpty()) {
                    Logger.error("Notes migration error: These old note files can't be deleted: " + cantBeDeleted.toString());
                }
            }
            result = "Migration done. Pages: " + n;
        } catch (Exception e) {
            Logger.error(e);
            result = e.getMessage();
        }
    }

    @Override
    protected String render() {
        return result;
    }

    private void migrateNote(SeiteSO seiteSO, Note n) {
        Comment c = toComment(n);
        files.put(seiteSO.getBook().getFolder() + "/comments/" + seiteSO.getId() + "/" + c.getId() + ".json", StringService.prettyJSON(c));
        deleteFiles.add(seiteSO.getBook().getFolder() + "/notes/" + seiteSO.getId() + "/" + n.getId() + ".json");
    }

    private Comment toComment(Note n) {
        Comment c = new Comment();
        c.setId(n.getId());
        c.setParentId(n.getParentId());
        c.setUser(n.getUser());
        c.setChanged(n.getChanged());
        c.setCreated(n.getCreated());
        c.setDone(n.isDone());
        c.setDoneDate(n.getDoneDate());
        if (n.getPersons() != null && n.getPersons().size() > 0) {
            c.setPerson(n.getPersons().get(0));
        }
        c.setText(newText(n));
        c.setVersion(1);
        return c;
    }

    private String newText(Note n) {
        String text = n.getText();
        if (text == null) {
            text = "";
        }
        text = text.replace("\r\n", "\n");
        while (text.startsWith("\n")) {
            text = text.substring(1);
        }
        while (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        text = includePersonsAndDoneBy(text, n);
        String html = "<p>" + text.replace("\n\n", "</p><p>").replace("\n", "<br/>") + "</p>";
        return StringService.prettyHTML(html.replace("<p></p>", "<p>&nbsp;</p>"));
    }

    private String includePersonsAndDoneBy(String text, Note n) {
        List<String> mignotes = new ArrayList<>();
        
        List<String> persons = n.getPersons();
        if (persons != null) {
            for (int i = 1/*omit first*/; i < persons.size(); i++) {
                String zust = persons.get(i);
                mignotes.add("außerdem zuständig: [@" + UserAccess.login2RealName(zust) + "]");
            }
        }
        
        boolean done = n.isDone();
        String doneBy = n.getDoneBy();
        if (done && !StringService.isNullOrEmpty(doneBy) && !(
                persons != null && persons.size() == 1 && doneBy.equals(persons.get(0))
                )) {
            mignotes.add("erledigt durch: " + UserAccess.login2RealName(doneBy));
        }
        
        if (!mignotes.isEmpty()) {
            text += "\n\nMigrationshinweise:";
        }
        for (String mignote : mignotes) {
            text += "\n" + mignote;
        }
        return text;
    }
}
