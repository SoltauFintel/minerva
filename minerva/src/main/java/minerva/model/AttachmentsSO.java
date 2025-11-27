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

import github.soltaufintel.amalia.base.FileService;
import gitper.access.CommitMessage;
import gitper.access.DirAccess;
import gitper.base.StringService;
import minerva.base.UserMessage;
import minerva.model.SearchSO.SearchContext;
import ohhtml.downloads.Attachment;
import ohhtml.downloads.GetAttachments;
import spark.utils.IOUtils;

public class AttachmentsSO {
    private static final String FOLDER = "attachments";
    private final String dir;
    private final SeiteSO seite;
    
    public AttachmentsSO(SeiteSO seite) {
        this.seite = seite;
        dir = seite.getBook().getFolder() + "/" + FOLDER + "/" + seite.getId();
    }
    
    public List<Attachment> list() {
        return GetAttachments.list(dir, getFilenames());
    }

    public boolean hasAttachments() {
        Set<String> filenames = getFilenames();
        return filenames != null && !filenames.isEmpty();
    }

    private Set<String> getFilenames() {
        return seite.getBook().dao().getFilenames(dir);
    }

    public Attachment get(String filename) {
        return list().stream()
                .filter(i -> i.getFilename().equals(filename))
                .findFirst().orElseThrow(() -> new UserMessage("attachmentDoesnotExist", seite.getBook().getWorkspace()));
    }
    
    public void save(InputStream inputStream, String dn, String category) throws FileAreadyExists {
        File file = new File(dir, dn);
        if (file.isFile()) {
            throw new FileAreadyExists();
        }
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, fos);
        } catch (IOException e) {
            Logger.error(e);
            throw new RuntimeException("Error uploading attachment!");
        }
        
        List<String> cat;
        if (StringService.isNullOrEmpty(category)) {
            cat = List.of("datei");
        } else {
            Attachment att = new Attachment(dn);
            att.fromString(category.toLowerCase());
            cat = att.getCategories();
        }

        Map<String, String> files = new HashMap<>();
        files.put(dir + "/" + dn, DirAccess.IMAGE);
        files.put(dir + "/" + dn + ".cat", StringService.isNullOrEmpty(category) ? "datei" : cat.stream().collect(Collectors.joining(",")));
        seite.getBook().dao().saveFiles(files,
                seite.commitMessage("save attachments: " + dn),
                seite.getBook().getWorkspace());
        Logger.info("saved attachment file as " + file.getAbsolutePath());
    }
    
    public static class FileAreadyExists extends Exception {
    }
    
    public void saveCategories(Attachment att) {
        Map<String, String> files = new HashMap<>();
        String dn = dir + "/" + att.getFilename() + ".cat";
        String content = att.getCategories().stream()
                .sorted((a, b) -> a.compareToIgnoreCase(b))
                .collect(Collectors.joining(","));
        files.put(dn, content);
        seite.getBook().dao().saveFiles(files,
                seite.commitMessage("save attachment categories: " + att.getFilename()),
                seite.getBook().getWorkspace());
    }
    
    public void delete(String filename) {
        Attachment att = get(filename);
        Set<String> filenames = new HashSet<>();
        String dn = dir + "/" + att.getFilename();
        filenames.add(dn);
        filenames.add(dn + ".cat");
        List<String> cantBeDeleted = new ArrayList<>();
        CommitMessage cm = seite.commitMessage("delete attachment: " + att.getFilename());
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
    
    public void publish(File targetFolder) {
        Set<String> filenames = getFilenames();
        if (filenames != null) {
            for (String dn : filenames) {
                FileService.copyFile(new File(dir, dn), new File(targetFolder, seite.getId()));
            }
        }        
    }

	public void search(SearchContext sc) {
		if ("de".equals(sc.getLang())) {
			Set<String> filenames = getFilenames();
			if (filenames != null) {
				final String x = sc.getX().toLowerCase();
				for (String filename : filenames) {
					// search by exact category
					if (filename.endsWith(".cat")) {
						String cat = FileService.loadPlainTextFile(new File(dir, filename));
						if (cat != null && cat.equalsIgnoreCase(x)) {
							sc.add(seite, "attachment category: " + cat).setIcon("fa-paperclip colatt");
						}
					} else {
						
						// search by filename
						String dn = new File(filename).getName();
						if (dn.toLowerCase().contains(x)) {
							sc.add(seite, "attachment: " + dn).setIcon("fa-paperclip colatt");
						}
					}
				}
			}
		}
	}
}
