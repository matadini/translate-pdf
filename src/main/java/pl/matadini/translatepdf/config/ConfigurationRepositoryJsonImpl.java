package pl.matadini.translatepdf.config;

import java.io.File;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Value;

@Value
class ConfigurationRepositoryJsonImpl implements ConfigurationRepository {

	String configFilePath;

	@Override
	public boolean createOrUpdate(Configuration entity) {

		boolean toReturn = true;
		try {
			Gson gson = createGson();
			String string = gson.toJson(entity);
			com.google.common.io.Files.write(string, new File(configFilePath), Charsets.UTF_8);
		} catch (Exception ex) {
			org.pmw.tinylog.Logger.info(ex);
			toReturn = false;
		}
		return toReturn;
	}

	private Gson createGson() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson;
	}

	@Override
	public Configuration read() {
		Configuration toReturn = null;
		try {
			File file = new File(configFilePath);
			if (!file.exists()) {
				throw new Exception("file doesnt exist");
			}

			String string = Files.toString(file, Charsets.UTF_8);
			if (Strings.isNullOrEmpty(string)) {
				throw new Exception("file content is empty");
			}

			Gson gson = this.createGson();
			toReturn = gson.fromJson(string, Configuration.class);

		} catch (Exception ex) {
			org.pmw.tinylog.Logger.info(ex);
		}
		return toReturn;
	}

}