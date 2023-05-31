package minerva.base;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

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
        savePlainTextFile(file, data == null ? null : new Gson().toJson(data));
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
    
    public static void deleteFolder(File folder) {
        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException ignore) {
        }
    }
}
