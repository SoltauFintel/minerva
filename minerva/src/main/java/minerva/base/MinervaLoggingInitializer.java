package minerva.base;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.writers.ConsoleWriter;
import org.pmw.tinylog.writers.RollingFileWriter;

import github.soltaufintel.amalia.web.builder.LoggingInitializer;

public class MinervaLoggingInitializer extends LoggingInitializer {
    
    public MinervaLoggingInitializer(Level level) {
        super(level);
    }

    @Override
    public void init() {
        // Es wird immer nach sysout geloggt.
        var c = Configurator.currentConfig().writer(new ConsoleWriter());
        if (!"0".equals(System.getenv("FILELOGGING"))) {
            // Das Loggen in eine Datei kann mit env var FILELOGGING=0 abgeschaltet werden.
            c.addWriter(new RollingFileWriter("/tmp/logs/minerva/minerva.log", 1, new TimestampLabeler("yyyy-MM-dd")));
        }
        Level level;
        try {
            level = Level.valueOf(System.getenv("LOGLEVEL").toUpperCase());
        } catch (Exception ignore) {
            level = Level.INFO;
        }
        c.formatPattern("{date} {level}  {message}")
            .level(level)
            .activate();
    }
}
