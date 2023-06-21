package minerva.base;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.writers.ConsoleWriter;

import github.soltaufintel.amalia.web.builder.LoggingInitializer;

public class MinervaLoggingInitializer extends LoggingInitializer {
    private final Level defaultLevel;

    public MinervaLoggingInitializer(Level level) {
        super(level);
        this.defaultLevel = level;
    }

    @Override
    public void init() {
        Level level;
        try {
            level = Level.valueOf(System.getenv("LOGLEVEL").toUpperCase());
        } catch (Exception ignore) {
            level = defaultLevel; // TODO Amalia getter
        }
        Configurator.currentConfig()
                .writer(new ConsoleWriter())
                .formatPattern("{date} {level}  {message}") // TODO Amalia  einfacher änderbar machen
                .level(level)
                .activate();
    }
}
