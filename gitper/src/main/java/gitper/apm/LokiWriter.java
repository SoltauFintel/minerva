package gitper.apm;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Configuration;
import org.pmw.tinylog.LogEntry;
import org.pmw.tinylog.writers.LogEntryValue;
import org.pmw.tinylog.writers.Writer;

/**
 * Asynchronous tinylog writer that forwards log entries to Loki.
 *
 * Logging threads never wait for Loki. Log entries are put into an internal
 * queue and sent by a dedicated background thread.
 */
public class LokiWriter implements Writer {
    private static final int QUEUE_SIZE = 10_000;
    private final BlockingQueue<LokiLogEntry> queue =
            new ArrayBlockingQueue<>(QUEUE_SIZE);
    private final LokiClient lokiClient;
    private volatile boolean running;
    private Thread writerThread;

    public LokiWriter(LokiClient lokiClient) {
        this.lokiClient = lokiClient;
    }

    @Override
    public Set<LogEntryValue> getRequiredLogEntryValues() {
        return EnumSet.of(
                LogEntryValue.LEVEL,
                LogEntryValue.RENDERED_LOG_ENTRY);
    }

    @Override
    public void init(Configuration configuration) throws IOException {
        running = true;

        writerThread = new Thread(this::run, "loki-writer");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    @Override
    public void write(LogEntry logEntry) throws IOException {
        LokiLogEntry entry = new LokiLogEntry(
                logEntry.getLevel().name(),
                logEntry.getRenderedLogEntry());

        if (!queue.offer(entry)) {
            System.err.println("Loki log queue is full. Log entry discarded.");
        }
    }

    private void run() {
        while (running || !queue.isEmpty()) {
            try {
                LokiLogEntry entry = queue.poll(1, TimeUnit.SECONDS);

                if (entry != null) {
                    lokiClient.log(entry.level(), entry.message());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                // Never use tinylog here, otherwise this writer would call itself.
                System.err.println("Could not send log entry to Loki: "
                        + e.getMessage());
            }
        }
    }

    @Override
    public void flush() throws IOException {
        // Nothing to do. Entries are sent continuously by the writer thread.
    }

    @Override
    public void close() throws IOException {
        running = false;

        if (writerThread != null) {
            try {
                writerThread.join(2_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private record LokiLogEntry(String level, String message) {
    }
}
