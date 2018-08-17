package pl.matadini.translatepdf.config;

interface ConfigurationRepository {

	boolean createOrUpdate(Configuration entity);

	Configuration read();
	
	
}
