package gitper.apm;

import java.util.Map;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.Meter;

public abstract class AbstractMetricsInstrument<I> {
	private static Meter meter;
	protected final String name;
	protected final String unit;
	protected String description;
	protected I instrument;
	
	protected Meter meter() {
		if (meter == null) {
			meter = GlobalOpenTelemetry.get().getMeter("minerva");
		}
		return meter;
	}
	
	public AbstractMetricsInstrument(String name, String unit) {
		this.name = name;
		this.unit = unit;
	}

	public AbstractMetricsInstrument(String name, String unit, String description) {
		this.name = name;
		this.unit = unit;
		this.description = description; 
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	protected Attributes labelsToAttributes(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return Attributes.empty();
        }
        AttributesBuilder builder = Attributes.builder();
        labels.forEach((k, v) -> {
            if (k != null && !k.isBlank() && v != null) {
                builder.put(AttributeKey.stringKey(k), v);
            }
        });
        return builder.build();
    }
}
