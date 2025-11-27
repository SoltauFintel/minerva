package minerva.base;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;

public class LogFile {
    private final File file;
    
    public LogFile(String dn) {
        file = new File(MinervaWebapp.factory().getConfig().getWorkspacesFolder(), dn);
        file.getParentFile().mkdirs();
    }
    
    public void write(String line) {
        try (FileWriter w = new FileWriter(file, true)) {
            w.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ")) + line + "\r\n");
        } catch (IOException e) {
            Logger.warn(e.getMessage() + " -> " + line);
        }
    }
}
