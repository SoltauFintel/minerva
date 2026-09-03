package gitper.base;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TimeLogger {
    private long prev;
    private final List<String> times = new ArrayList<>();

    public TimeLogger() {
        prev = System.currentTimeMillis();
    }

    public void measure(String name) {
        long time = System.currentTimeMillis() - prev;
        times.add(name + "=" + time + "ms");
        prev = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return times.stream().collect(Collectors.joining(", "));
    }
}
