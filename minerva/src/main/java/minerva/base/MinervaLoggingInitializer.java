package minerva.base;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.writers.ConsoleWriter;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.web.builder.LoggingInitializer;
import gitper.apm.LokiClient;
import gitper.apm.LokiWriter;

public class MinervaLoggingInitializer extends LoggingInitializer {
    
    public MinervaLoggingInitializer(Level level) {
        super(level);
    }

    @Override
    public void init() {
        var c = Configurator.currentConfig().writer(new ConsoleWriter());
        
        String application = System.getenv("LOG_APP");
        if (StringService.isNullOrEmpty(application)) {
            application = "minerva";
        }
        String host = System.getenv("LOKI");
        if (StringService.isNullOrEmpty(host)) {
            host = "http://grafana:3100";
        }
        if (!"-".equals(application)) {
            c.addWriter(new LokiWriter(new LokiClient(application, host)));
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
