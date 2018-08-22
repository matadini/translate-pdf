package pl.matadini.translatepdf.gui.config;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.cloud.translate.Language;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import pl.matadini.translatepdf.Main;
import pl.matadini.translatepdf.config.Configuration;
import pl.matadini.translatepdf.config.ConfigurationHandler;
import pl.matadini.translatepdf.gui.common.CommonUtil;

public class ConfigurationPaneController extends BorderPane {

	@FXML
	private Button buttonSelect;

	@FXML
	private ComboBox<Language> comboboxSourceLanguage;

	@FXML
	private ComboBox<Language> comboboxTargetLanguage;

	@FXML
	private Button buttonCancel;

	@FXML
	private Button buttonApply;

	@FXML
	private TextField textFieldJsonPath;

	private final ConfigurationPaneData.Input args;

	public ConfigurationPaneController(ConfigurationPaneData.Input args) {
		this.args = args;
	}

	@FXML
	void initialize() {
		initializeComboboxContent();
		initializeButtonEvents();
		fillUiUsingDataFromConfiguration();
	}

	private void initializeButtonEvents() {
		buttonSelect.setOnAction(ConfigurationPaneController.this::onClickButtonSelect);
		buttonCancel.setOnAction(ConfigurationPaneController.this::onClickButtonCancel);
		buttonApply.setOnAction(ConfigurationPaneController.this::onClickButtonApply);
	}

	private void fillUiUsingDataFromConfiguration() {
		Configuration configuration = ConfigurationHandler.INSTANCE.getConfiguration();
		List<Language> languages = args.getLanguages();
		if (Objects.nonNull(languages)) {
			CommonUtil.setDefaultLanguage(comboboxSourceLanguage, configuration.getDefaultSourceLanguage(), languages);
			CommonUtil.setDefaultLanguage(comboboxTargetLanguage, configuration.getDefaultTargetLanguage(), languages);
		}
		textFieldJsonPath.setText(configuration.getGoogleCredentialsJsonPath());
	}

	private void initializeComboboxContent() {
		try {
			if (!Objects.nonNull(args)) {
				throw new Exception("args is null");
			}

			List<Language> languages = args.getLanguages();
			if (!Objects.nonNull(languages)) {
				throw new Exception("languages is null");
			}

			comboboxSourceLanguage.setItems(FXCollections.observableArrayList(languages));
			comboboxTargetLanguage.setItems(FXCollections.observableArrayList(languages));

		} catch (Exception ex) {
			org.pmw.tinylog.Logger.info(ex.getMessage());
		}
	}

	private void onClickButtonApply(ActionEvent value) {
		updateConfiguration();
		args.getStage().close();
	}

	private void updateConfiguration() {
		Configuration configuration = getNewConfigurationFromUi();
		ConfigurationHandler.INSTANCE.update(configuration);
	}

	private Configuration getNewConfigurationFromUi() {
		Configuration configuration = new Configuration();

		Language selectedItemSource = comboboxSourceLanguage.getSelectionModel().getSelectedItem();
		Optional.ofNullable(selectedItemSource)
				.ifPresent(item -> configuration.setDefaultSourceLanguage(item.getCode()));

		Language selectedItemTarget = comboboxTargetLanguage.getSelectionModel().getSelectedItem();
		Optional.ofNullable(selectedItemTarget)
				.ifPresent(item -> configuration.setDefaultTargetLanguage(item.getCode()));

		configuration.setGoogleCredentialsJsonPath(textFieldJsonPath.getText());
		return configuration;
	}

	private void onClickButtonCancel(ActionEvent value) {
		args.getStage().close();
	}

	private void onClickButtonSelect(ActionEvent value) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
		File showOpenDialog = fileChooser.showOpenDialog(Main.getPrimaryStageInstance());
		if (Objects.nonNull(showOpenDialog)) {
			String path = showOpenDialog.getAbsoluteFile().getAbsolutePath();
			textFieldJsonPath.setText(path);
		}
	}
}
