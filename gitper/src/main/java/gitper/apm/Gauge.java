package gitper.apm;

import java.util.Map;

import gitper.base.StringService;
import io.opentelemetry.api.metrics.LongGauge;

public class Gauge extends AbstractMetricsInstrument<LongGauge> {

    /** unit "1" */
    public Gauge(String name) {
        this(name, "1");
    }

    public Gauge(String name, String unit) {
        super(name, unit);
    }

    public void set(long value) {
        set(value, null);
    }

    public void set(long value, Map<String, String> labels) {
        if (instrument == null) {
            var b = meter().gaugeBuilder(name).ofLongs().setUnit(unit);
            if (!StringService.isNullOrEmpty(description)) {
                b = b.setDescription(description);
            }
            instrument = b.build();
        }
        instrument.set(value, labelsToAttributes(labels));
    }
}
