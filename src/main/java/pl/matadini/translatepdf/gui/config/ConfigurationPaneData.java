package pl.matadini.translatepdf.gui.config;

import java.util.List;

import com.google.cloud.translate.Language;

import javafx.stage.Stage;
import lombok.Builder;
import lombok.Value;
import pl.matadini.translatepdf.gui.config.ConfigurationPaneData.Input;
import pl.matadini.translatepdf.gui.config.ConfigurationPaneData.Output;
import pl.matadini.translatepdf.gui.config.ConfigurationPaneData.Input.InputBuilder;
import pl.matadini.translatepdf.gui.config.ConfigurationPaneData.Output.OutputBuilder;

public class ConfigurationPaneData {

	@Value
	@Builder
	public static class Input {
		List<Language> languages;
		Stage stage;
	}

	@Value
	@Builder
	public static class Output {

	}
}