package minerva.config;

import org.pmw.tinylog.Logger;

public class ConfigurationException extends RuntimeException {

	/**
	 * User just gets output 'Configuration error'.
	 * @param internalMsg message will be logged as error
	 */
	public ConfigurationException(String internalMsg) {
		super("Configuration error");
		Logger.error(internalMsg);
	}
}
