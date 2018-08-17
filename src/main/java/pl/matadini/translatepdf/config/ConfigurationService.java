package pl.matadini.translatepdf.config;

import java.util.Objects;

public class ConfigurationService {

	public Configuration getConfiguration() {
		Configuration read = repository.read();
		if (Objects.isNull(read)) {
			read = new Configuration();
			repository.createOrUpdate(read);
		}
		return read;
	}

	public boolean update(Configuration configuration) {
		return repository.createOrUpdate(configuration);
	}

	public static final ConfigurationService INSTANCE = new ConfigurationService();

	private final ConfigurationRepository repository;

	private ConfigurationService() {
		repository = new ConfigurationRepositoryJsonImpl("C://pdf//config.json");
	}
}
