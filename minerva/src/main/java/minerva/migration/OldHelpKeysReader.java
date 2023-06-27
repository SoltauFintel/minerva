package minerva.migration;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

public class OldHelpKeysReader {
    private static final String jiraUrl = "http://jira01.intern.x-map.de:8090/pages/viewpage.action?pageId=";
    private boolean lastLineIsGO;
    private final List<String> block = new ArrayList<>();
    private final Map<String, List<String>> helpInfos = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        new OldHelpKeysReader().readMappings(new File("help-keys"));
    }
    
    public void readMappings(File mappingsDir) throws Exception {
        File[] files = mappingsDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String content = new String(Files.readAllBytes(file.toPath()), Charset.forName("windows-1252"));
            lastLineIsGO = true;
            for (String line : content.split("\n")) {
                if (line.trim().startsWith("de:") || line.trim().startsWith("en:") || line.isBlank()
                        || line.trim().startsWith("<<")) { // lang line or ignored line
                    checkIfNewBlock();
                } else { // GO line
                    lastLineIsGO = true;
                }
                block.add(line);
            }
            checkIfNewBlock();
        }
        Logger.info("help keys map: " + helpInfos.size());
    }
    
    private void checkIfNewBlock() {
        if (lastLineIsGO) {
            // new block begins with this line, but first process previous block
            if (!block.isEmpty()) {
                work();
            }
            block.clear();
            lastLineIsGO = false;
        }
    }

    private void work() {
        // collect data
        String enPageId = null;
        String dePageId = null;
        List<String> helpKeys = new ArrayList<>();
        for (String i : block) {
            if (i.startsWith("de:")) {
                String line = i.substring("de:".length()).trim();
                if (line.startsWith(jiraUrl)) {
                    dePageId = line.substring(jiraUrl.length());
                }
            } else if (i.startsWith("en:")) {
                String line = i.substring("en:".length()).trim();
                if (line.startsWith(jiraUrl)) {
                    enPageId = line.substring(jiraUrl.length());
                }
            } else if (!i.isBlank()) {
                helpKeys.add(i.trim());
            }
        }
        // save
        saveHelpKeys(helpKeys, dePageId);
        saveHelpKeys(helpKeys, enPageId);
    }

    private void saveHelpKeys(List<String> helpKeys, String pageId) {
        if (!helpKeys.isEmpty() && pageId != null) {
            List<String> x = helpInfos.get(pageId);
            if (x == null) {
                helpInfos.put(pageId, helpKeys);
            } else {
                x.addAll(helpKeys);
            }
        }
    }

    public List<String> getHelpKeys(String pageId) {
        return helpInfos.get(pageId);
    }
}
