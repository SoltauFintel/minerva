package minerva.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Option {
	private String key;
	private String label;
	private OptionType type;
	private String defaultValue;
	private String hint;

	public Option() {
	}

	public Option(String key, String label, OptionType type) {
		this.key = key;
		this.label = label;
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public OptionType getType() {
		return type;
	}

	public void setType(OptionType type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public Option setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public String getHint() {
		return hint;
	}

	public Option setHint(String hint) {
		this.hint = hint;
		return this;
	}

	public void validate() {
	}
	
	public String get() {
		String val = MinervaOptions.options.optionValues.get(key);
		return val == null ? defaultValue : val;
	}

	public void set(String value) {
		MinervaOptions.options.optionValues.put(key, value);
	}

	public boolean bool() {
		return "true".equals(get());
	}

	public void bool(boolean value) {
		set(value ? "true" : "false");
	}
	
	public List<String> list() {
		String list = get();
		if (list == null) {
			return new ArrayList<>();
		}
		return Arrays.asList(list.split(","));
	}
	
	public void list(List<String> values) {
		set(values == null ? null : values.stream().collect(Collectors.joining(",")));
	}
}
