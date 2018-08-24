package pl.matadini.translatepdf.config;

public interface ConfigurationService {

	Configuration getConfiguration();

	boolean update(Configuration configuration);

	static ConfigurationService getDefault() {
		return ConfigurationHandler.INSTANCE;
	}

}