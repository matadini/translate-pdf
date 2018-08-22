package pl.matadini.translatepdf.config;

import java.util.Objects;

class ConfigurationServiceImpl implements ConfigurationService {

	private final String CONFIG_FILE_NAME = "config.json";

	private final ConfigurationRepository repository;

	ConfigurationServiceImpl() {
		String configFilePath = System.getProperty("user.home") + "//" + CONFIG_FILE_NAME;
		repository = new ConfigurationRepositoryJsonImpl(configFilePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.matadini.translatepdf.config.ConfigurationService#getConfiguration()
	 */
	@Override
	public Configuration getConfiguration() {
		Configuration read = repository.read();
		if (Objects.isNull(read)) {
			read = new Configuration();
			repository.createOrUpdate(read);
		}
		return read;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.matadini.translatepdf.config.ConfigurationService#update(pl.matadini.
	 * translatepdf.config.Configuration)
	 */
	@Override
	public boolean update(Configuration configuration) {
		return repository.createOrUpdate(configuration);
	}
}
