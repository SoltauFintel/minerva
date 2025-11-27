package minerva.base;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import github.soltaufintel.amalia.base.FileService;
import minerva.MinervaWebapp;

public class Sequence {
    private static final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();
    private final String key;
    private final Object keylock;

    public Sequence(String key) {
        this.key = key;
        keylock = locks.computeIfAbsent(key, k -> new Object());
    }

    public int nextNumber() {
        synchronized (keylock) {
            File file = new File(MinervaWebapp.factory().getConfig().getWorkspacesFolder() + "/" + key + ".sequence");
            String content = FileService.loadPlainTextFile(file);
            if (content == null) {
                content = "0";
            }
            int value = Integer.parseInt(content) + 1;
            FileService.savePlainTextFile(file, "" + value);
            return value;
        }
    }
}
