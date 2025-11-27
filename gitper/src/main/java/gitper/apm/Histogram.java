package gitper.apm;

import java.util.List;
import java.util.Map;

import gitper.base.StringService;
import io.opentelemetry.api.metrics.LongHistogram;

public class Histogram extends AbstractMetricsInstrument<LongHistogram> {
    private final List<Long> buckets;
    
    public Histogram(String name, String unit, List<Long> buckets) {
        super(name, unit);
        this.buckets = buckets;
    }

    public void record(long value) {
        record(value, null);
    }

    public void record(long value, Map<String, String> labels) {
        if (instrument == null) {
            var b = meter().histogramBuilder(name).ofLongs().setUnit(unit);
            if (!StringService.isNullOrEmpty(description)) {
                b = b.setDescription(description);
            }
            instrument = b.setExplicitBucketBoundariesAdvice(buckets).build();
        }
        instrument.record(value, labelsToAttributes(labels));
    }
}
