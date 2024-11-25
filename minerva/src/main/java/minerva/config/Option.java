package minerva.config;

import org.pmw.tinylog.Logger;

import gitper.base.StringService;

public class Option {
	private String key;
	private String label;
	private OptionType type;
	private String defaultValue;
	private String hint;
	private boolean notForCustomerVersion = false;

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

	public String get() {
		String val = MinervaOptions.options.optionValues.get(key);
		return val == null ? defaultValue : val;
	}
	
	/**
	 * @return same as get(), but if value is null or empty throw an Exception
	 */
	public String notEmpty() {
		String ret = get();
		if (StringService.isNullOrEmpty(ret)) {
			Logger.error("Option \"" + key + "\" is not set");
			throw new RuntimeException("This function is not available due to a configuration problem. Please contact the administrator.");
		}
		return ret;
	}
	
	public boolean isSet() {
		return !StringService.isNullOrEmpty(MinervaOptions.options.optionValues.get(key));
	}

	/**
	 * Call save() to persist change.
	 * @param value -
	 */
	public void set(String value) {
		MinervaOptions.options.optionValues.put(key, value);
	}
	
	public Option notForCustomerVersion() {
//	TODO bug	notForCustomerVersion = true;
		return this;
	}

	public boolean isNotForCustomerVersion() {
		return notForCustomerVersion;
	}

//	public boolean bool() {
//		return "true".equals(get());
//	}
//
//	/**
//	 * Call save() to persist change.
//	 * @param value -
//	 */
//	public void bool(boolean value) {
//		set(value ? "true" : "false");
//	}
//	
//	public List<String> list() {
//		String list = get();
//		if (list == null) {
//			return new ArrayList<>();
//		}
//		return Arrays.asList(list.split(","));
//	}
//	
//	public void list(List<String> values) {
//		set(values == null ? null : values.stream().collect(Collectors.joining(",")));
//	}
}
