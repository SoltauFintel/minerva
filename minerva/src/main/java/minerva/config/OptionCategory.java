package minerva.config;

import java.util.ArrayList;
import java.util.List;

public class OptionCategory {
	private String label;
	private final List<Option> options = new ArrayList<>();

	public OptionCategory() {
	}

	public OptionCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Option> getOptions() {
		return options;
	}
	
	public Option add(String key, String label) {
		return add(new Option(key, label, OptionType.TEXT));
	}

	public Option add(String key, String label, OptionType type) {
		return add(new Option(key, label, type));
	}
	
	public Option add(Option option) {
		options.add(option);
		return option;
	}

	public Option getOption(String key) {
		for (Option o : options) {
			if (o.getKey().equals(key)) {
				return o;
			}
		}
		return null;
	}
}
