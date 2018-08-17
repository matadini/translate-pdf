package pl.matadini.translatepdf.gui.common;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.cloud.translate.Language;
import com.google.common.base.Strings;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;

public class CommonUtil {

	public static void setDefaultLanguage(ComboBox<Language> combobox, String languageCode, List<Language> languages) {
		if (!Strings.isNullOrEmpty(languageCode)) {
			Optional<Language> findFirst = languages.stream()
					.filter(item -> item.getCode().equals(languageCode))
					.findFirst();
			if (findFirst.isPresent()) {
				combobox.getSelectionModel().select(findFirst.get());
			}
		}
	}

	public static <T> T loadFxml(T controller, URL resource) throws IOException {

		FXMLLoader loader = new FXMLLoader(resource);
		loader.setRoot(controller);
		loader.setController(controller);
		loader.load();
		return controller;
	}

	private CommonUtil() {

	}
}