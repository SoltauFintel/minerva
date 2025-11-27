package gitper.apm;

import java.util.Map;

import gitper.base.StringService;
import io.opentelemetry.api.metrics.LongCounter;

public class Counter extends AbstractMetricsInstrument<LongCounter> {

    /** unit "1" */
    public Counter(String name) {
        this(name, "1");
    }

    public Counter(String name, String unit) {
        super(name, unit);
    }

    public void inc() {
        add(1, null);
    }
    
    public void add(long value) {
        add(value, null);
    }

    public void add(long value, Map<String, String> labels) {
        if (instrument == null) {
            var b = meter().counterBuilder(name).setUnit(unit);
            if (!StringService.isNullOrEmpty(description)) {
                b = b.setDescription(description);
            }
            instrument = b.build();
        }
        instrument.add(value, labelsToAttributes(labels));
    }
}
