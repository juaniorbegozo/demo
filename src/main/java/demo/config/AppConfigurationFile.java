package demo.config;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;

public class AppConfigurationFile implements PropertyFileConfig {

	@Override
	public String getPropertyFileName() {
		return "application.properties";
	}

	@Override
	public boolean isOptional() {		
		return false;
	}

}
