package minerva.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.attachments.Attachment;
import minerva.base.FileService;
import minerva.base.UserMessage;
import spark.utils.IOUtils;

public class AttachmentsSO {
    private static final String FOLDER = "attachments";
    private final String dir;
    private final SeiteSO seite;
    
    // TODO Volltextsuche sollte auch nach Attachment-filename/-categories suchen können
    
    public AttachmentsSO(SeiteSO seite) {
        this.seite = seite;
        dir = seite.getBook().getFolder() + "/" + FOLDER + "/" + seite.getId();
    }
    
    public List<Attachment> list() {
        Map<String, Attachment> map = new HashMap<>();
        Set<String> filenames = seite.getBook().dao().getFilenames(dir);
        if (filenames != null) {
            // first collect attachment files
            for (String dn : filenames) {
                if (!dn.endsWith(".cat")) { // attachment file
                    map.put(dn, new Attachment(dn));
                }
            }
            // then add categories to the attachment files
            for (String dn : filenames) {
                if (dn.endsWith(".cat")) { // categories file
                    String content = FileService.loadPlainTextFile(new File(dir, dn));
                    dn = dn.substring(0, dn.length() - ".cat".length());
                    Attachment att = map.get(dn);
                    if (att != null) {
                        att.fromString(content);
                    } // else: categories without attachment -> ignore it
                }
            }
        }
        List<Attachment> ret = new ArrayList<>(map.values());
        ret.sort((a, b) -> a.getFilename().compareToIgnoreCase(b.getFilename()));
        return ret;
    }

    public Attachment get(String filename) {
        return list().stream()
                .filter(i -> i.getFilename().equals(filename))
                .findFirst().orElseThrow(() -> new UserMessage("attachmentDoesnotExist", seite.getBook().getWorkspace()));
    }
    
    public void save(InputStream inputStream, String dn) {
        File file = new File(dir, dn);
        if (file.isFile()) {
            throw new RuntimeException("Datei bereits vorhanden!"); // TODO UserMessage, bzw. sichtbare Meldung für Anwender
        }
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, fos);
        } catch (IOException e) {
            Logger.error(e);
            throw new RuntimeException("Error uploading attachment!");
        }

        Map<String, String> files = new HashMap<>();
        files.put(dir + "/" + dn, DirAccess.IMAGE);
        files.put(dir + "/" + dn + ".cat", "datei");
        seite.getBook().dao().saveFiles(files,
                new CommitMessage(seite, "save attachments: " + dn),
                seite.getBook().getWorkspace());
        Logger.info("saved attachment file as " + file.getAbsolutePath());
    }
    
    public void saveCategories(Attachment att) {
        Map<String, String> files = new HashMap<>();
        String dn = dir + "/" + att.getFilename() + ".cat";
        String content = att.getCategories().stream()
                .sorted((a, b) -> a.compareToIgnoreCase(b))
                .collect(Collectors.joining(";"));
        files.put(dn, content);
        seite.getBook().dao().saveFiles(files,
                new CommitMessage(seite, "save attachment categories: " + att.getFilename()),
                seite.getBook().getWorkspace());
    }
    
    public void delete(String filename) {
        Attachment att = get(filename);
        Set<String> filenames = new HashSet<>();
        String dn = dir + "/" + att.getFilename();
        filenames.add(dn);
        filenames.add(dn + ".cat");
        List<String> cantBeDeleted = new ArrayList<>();
        CommitMessage cm = new CommitMessage(seite, "delete attachment: " + att.getFilename());
        seite.getBook().dao().deleteFiles(filenames, cm, seite.getBook().getWorkspace(), cantBeDeleted);
        if (cantBeDeleted.isEmpty()) {
            Logger.info("Attachment deleted: " + att.getFilename());
        } else {
            Logger.error("Error deleting attachment \"" + att.getFilename() + "\". Can't be deleted: " + cantBeDeleted);
        }
        
        // Nach dem Löschen des letzten Attachments soll auch der Ordner gelöscht werden.
        Set<String> filenames2 = seite.getBook().dao().getFilenames(dir);
        if (filenames2 != null && filenames2.isEmpty()) {
            new File(dir).delete();
        }
    }
}
