package gitper.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import gitper.Workspace;
import gitper.access.CommitMessage;

public class FileService {

    private FileService() {
    }
    
    public static List<String> listFolders(File folder) {
        List<String> ret = new ArrayList<>();
        File[] folders = folder.listFiles();
        if (folders != null) {
            for (File f : folders) {
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    ret.add(f.getName());
                }
            }
        }
        return ret;
    }
    
    public static String loadPlainTextFile(File file) {
        if (file.isFile()) {
            try {
                return new String(Files.readAllBytes(file.toPath()));
            } catch (Exception e) {
                Logger.error(file.getAbsolutePath());
                throw new RuntimeException("Error loading file", e);
            }
        }                
        return null;
    }

    public static void savePlainTextFile(File file, String content) {
        if (content == null) {
            file.delete();
        } else {
            file.getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(file)) {
                w.write(content);
            } catch (IOException e) {
                Logger.error(file.getAbsolutePath());
                throw new RuntimeException("Error saving file", e);
            }
        }
    }

    public static <T> T loadJsonFile(File file, Class<T> type) {
        String json = loadPlainTextFile(file);
        return json == null ? null : new Gson().fromJson(json, type);
    }

    public static <T> void saveJsonFile(File file, T data) {
        savePlainTextFile(file, data == null ? null : prettyJSON(data));
    }
    
    public static boolean isLegalFilename(String filename) {
        return filename != null && !filename.isBlank()
                && !filename.contains("\\")
                && !filename.contains("/")
                && !filename.contains(":")
                && !filename.contains("*")
                && !filename.contains("?")
                && !filename.contains("\"")
                && !filename.contains("<")
                && !filename.contains(">")
                && !filename.contains("|");
    }
    
    public static String getSafeName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        String ret = "";
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            switch (c) {
            case '\\':
            case '/':
            case ':':
            case '*':
            case '?':
            case '"':
            case '<':
            case '>':
            case '|':
                break;
            case ' ':
                ret += '-';
                break;
            default:
                ret += c;
            }
        }
        ret = ret.replace("--", "-");
        if (ret.isEmpty()) {
            throw new RuntimeException("Safe name is empty");
        }
        return ret;
    }

    public static void deleteFolder(File folder) {
        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException ignore) {
        }
    }
    
    public static void copyFile(File fromFile, File toDir) {
        try {
            toDir.mkdirs();
            Files.copy(fromFile.toPath(), new File(toDir, fromFile.getName()).toPath(), //
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Logger.error(e);
            throw new RuntimeException("Publish error. See log.");
        }
    }
    
    public static void copyFiles(File fromDir, File toDir) {
        File[] files = fromDir.listFiles();
        if (files != null) {
            for (File file : files) {
                copyFile(file, toDir);
            }
        }
    }

    public static void moveFiles(File fromDir, File toDir) {
        File[] files = fromDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                toDir.mkdirs();
                try {
                    Files.move(file.toPath(), new File(toDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    Logger.error(e);
                    throw new RuntimeException("Can't move file. See log.");
                }
            } else if (file.isDirectory()) {
                moveFiles(file, new File(toDir, file.getName()));
            }
        }
    }

    public static void zip(File folder, File zipFile) {
        zipFile.delete();
        int startOfFilenameWithRelativePath = folder.getAbsolutePath().length() + 1;
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    out.putNextEntry(new ZipEntry(path.toFile().getAbsolutePath()
                            .substring(startOfFilenameWithRelativePath)));
                    Files.copy(path, out);
                    out.closeEntry();
                    return super.visitFile(path, attrs);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Zip given files to zipFile. Duplicate filenames are not supported.
     * @param files to add to zip file
     * @param zipFile -
     */
    public static void zip(List<File> files, File zipFile) {
        zipFile.delete();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : files) {
                out.putNextEntry(new ZipEntry(file.getName()));
                Files.copy(file.toPath(), out);
                out.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call only in Gitlab mode!
     * @param files to be backuped
     * @param backupDir target directory in workspace
     * @param workspace -
     * @return message
     */
    public static String backup(List<File> files, File backupDir, Workspace workspace) {
		Map<String, String> map = new HashMap<>();
		for (File file : files) {
			if (file.isFile()) {
				String content = loadPlainTextFile(file);
				File target = new File(backupDir, file.getName());
				if (!target.isFile() || !content.equals(loadPlainTextFile(target))) {
					map.put(target.getAbsolutePath(), content);
					Logger.debug("File backup: " + file.getAbsolutePath() + " -> " + target.getAbsolutePath());
				} else {
					Logger.debug("Target file is already uptodate: " + target.getAbsolutePath());
				}
			} else {
				Logger.debug("File not found: " + file.getAbsolutePath());
			}
		}
		if (map.isEmpty()) {
			return "files not found or not changed -> no backup!";
		}
		workspace.dao().saveFiles(map, new CommitMessage("backup"), workspace);
		return "backup made for " + map.size() + " file" + (map.size() == 1 ? "" : "s");
	}

    public static String prettyJSON(String json) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement je = JsonParser.parseString(json);
            return gson.toJson(je);
        } catch (JsonSyntaxException e) {
            Logger.error(e);
            return json;
        }
    }

    public static <T> String prettyJSON(T data) {
        return prettyJSON(new Gson().toJson(data));
    }
}
