package pl.matadini.translatepdf.gui.main;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.util.PropertiesManager;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Language;
//Imports the Google Cloud client library
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.matadini.translatepdf.Main;
import pl.matadini.translatepdf.config.Configuration;
import pl.matadini.translatepdf.config.ConfigurationHandler;
import pl.matadini.translatepdf.gui.common.CommonConst;
import pl.matadini.translatepdf.gui.common.CommonUtil;
import pl.matadini.translatepdf.gui.config.ConfigurationPaneController;
import pl.matadini.translatepdf.gui.config.ConfigurationPaneData;
import pl.matadini.translatepdf.gui.config.ConfigurationPaneData.Input;

public class MainPaneController extends BorderPane {

	@FXML
	private ComboBox<Language> comboboxSource;

	@FXML
	private ComboBox<Language> comboboxTarget;

	@FXML
	private MenuItem menuItemOpenPdf;

	@FXML
	private MenuItem menuItemsSettings;

	@FXML
	private MenuItem menuItemClose;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private TextArea testAreaSource;

	@FXML
	private TextArea testAreaResult;

	private SwingController pdfDocumentController;

	private JPanel pdfViewPanel;

	private ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

	private List<Language> listSupportedLanguages;

	@FXML
	private Label labelStatus;

	/**
	 * To fix IcePdf resize issues
	 * 
	 * @param scene
	 */
	public void bindScene(Scene scene) {
		scene.widthProperty().addListener((observable, oldValue, newValue) -> {
			SwingUtilities.invokeLater(() -> {
				pdfViewPanel.setSize(new Dimension(newValue.intValue(), (int) scene.getHeight()));
				pdfViewPanel.setPreferredSize(new Dimension(newValue.intValue(), (int) scene.getHeight()));
				pdfViewPanel.repaint();
			});
		});

		scene.heightProperty().addListener((observable, oldValue, newValue) -> {
			SwingUtilities.invokeLater(() -> {
				pdfViewPanel.setSize(new Dimension((int) scene.getWidth(), newValue.intValue()));
				pdfViewPanel.setPreferredSize(new Dimension((int) scene.getWidth(), newValue.intValue()));
				pdfViewPanel.repaint();
			});
		});
	}

	String getSelectedTextFromPdf() {
		String selected =  CommonConst.STRING_EMPTY; 
		if (Objects.nonNull(pdfDocumentController)) {
			try {
				int currentPageNumber = pdfDocumentController.getCurrentPageNumber();
				Document document = pdfDocumentController.getDocument();
				PageText pageText = document.getPageText(currentPageNumber);
				selected = pageText.getSelected().toString();
			} catch (Exception ex) {
				org.pmw.tinylog.Logger.info(ex);
			}
		}
		return selected;
	}

	private void setDefaultLanguage(ComboBox<Language> combobox, String languageCode) {
		CommonUtil.setDefaultLanguage(combobox, languageCode, listSupportedLanguages);
	}

	private void getSupportedLanguages() {

		javafx.application.Platform.runLater(() -> labelStatus.setText(CommonConst.STRING_DOWNLOAD_DATA));
		ListenableFuture<List<Language>> task = service.submit(() -> {

			List<Language> toReturn = null;
			try {
				Translate translateWithAuth = getTranslateWithAuth();
				if (Objects.isNull(translateWithAuth)) {
					throw new Exception(CommonConst.STRING_GOOGLE_API_FAIL);
				}

				toReturn = translateWithAuth.listSupportedLanguages();
				if (Objects.isNull(listSupportedLanguages)) {
					throw new Exception(CommonConst.STRING_LANGUAGE_LIST_FAIL);
				}

			} catch (Exception ex) {
				javafx.application.Platform.runLater(() -> labelStatus.setText(ex.getMessage()));
			}
			return toReturn;
		});

		task.addListener(() -> {
			try {
				List<Language> result = task.get();
				if (Objects.nonNull(result)) {
					listSupportedLanguages = result;
					javafx.application.Platform.runLater(() -> {
						initializeComboboxItemContent();
						selectComboboxItemsUsingConfig();
						labelStatus.setText(CommonConst.STRING_DOWNLOAD_DATA_FINISH);
					});

				}
			} catch (Exception ex) {
				org.pmw.tinylog.Logger.info(ex);
			}
		}, MoreExecutors.directExecutor());

	}

	private void initializeComboboxItemContent() {
		comboboxSource.setItems(FXCollections.observableArrayList(listSupportedLanguages));
		comboboxTarget.setItems(FXCollections.observableArrayList(listSupportedLanguages));
	}

	private void selectComboboxItemsUsingConfig() {
		Configuration configuration = ConfigurationHandler.INSTANCE.getConfiguration();
		setDefaultLanguage(comboboxSource, configuration.getDefaultSourceLanguage());
		setDefaultLanguage(comboboxTarget, configuration.getDefaultTargetLanguage());
	}

	private Translate getTranslateWithAuth() {
		Translate translate = null;
		try {
			Configuration configuration = ConfigurationHandler.INSTANCE.getConfiguration();
			String path = configuration.getGoogleCredentialsJsonPath();

			GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(path))
					.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

			translate = TranslateOptions.newBuilder()
					.setCredentials(credentials)
					.build()
					.getService();
		} catch (Exception ex) {
			org.pmw.tinylog.Logger.info(ex.getMessage());
		}
		return translate;
	}

	private void onClickMenuItemOpenPdf(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
		File showOpenDialog = fileChooser.showOpenDialog(Main.getPrimaryStageInstance());
		if (Objects.nonNull(showOpenDialog)) {
			pdfDocumentController.openDocument(showOpenDialog.getAbsolutePath());
		}
	}

	private void onClickMenuItemClose(ActionEvent event) {
		pdfDocumentController.closeDocument();
		Main.getPrimaryStageInstance().close();
	}

	private void onClickMenuItemSettings(ActionEvent event) {
		try {

			showConfigurationStage();

			if (Objects.isNull(listSupportedLanguages)) {
				getSupportedLanguages();
			} else {
				selectComboboxItemsUsingConfig();
			}

		} catch (Exception ex) {
			org.pmw.tinylog.Logger.info(ex);
		}
	}

	private void showConfigurationStage() throws IOException {
		Stage stage = new Stage();

		Input build = ConfigurationPaneData.Input.builder()
				.languages(listSupportedLanguages)
				.stage(stage)
				.build();

		URL resource = ConfigurationPaneController.class.getResource("ConfigurationPane.fxml");
		ConfigurationPaneController controller = CommonUtil.loadFxml(new ConfigurationPaneController(build), resource);
		Scene scene = new Scene(controller);

		stage.setScene(scene);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(Main.getPrimaryStageInstance());
		stage.showAndWait();
	}

	@FXML
	private void initialize() {

		initializePdfComponents();
		getSupportedLanguages();

		menuItemClose.setOnAction(MainPaneController.this::onClickMenuItemClose);
		menuItemOpenPdf.setOnAction(MainPaneController.this::onClickMenuItemOpenPdf);
		menuItemsSettings.setOnAction(MainPaneController.this::onClickMenuItemSettings);
	}

	private void initializePdfComponents() {

		pdfDocumentController = new SwingController();

		/**
		 * System properties
		 */
		Properties sysPoperties = System.getProperties();

		/**
		 * IcePdf properties
		 */
		PropertiesManager properties = new PropertiesManager(sysPoperties,
				ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
		properties.set("application.toolbar.show.rotate", "false");
		properties.set("application.toolbar.show.annotation", "false");
		properties.set("application.toolbar.show.utility", "false");

		SwingViewBuilder builder = new SwingViewBuilder(pdfDocumentController, properties);

		pdfViewPanel = builder.buildViewerPanel();
		SwingNode value = new SwingNode();
		value.setContent(pdfViewPanel);
		scrollPane.setContent(value);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(() -> pdfViewPanel.repaint());
			}
		}, 1000);
	}

	public void translate() {

		try {
			String selected = getSelectedTextFromPdf();
			if (Strings.isNullOrEmpty(selected)) {
				throw new Exception("text no selected");
			}

			Language sourceLanguage = comboboxSource.getSelectionModel().getSelectedItem();
			if (Objects.isNull(sourceLanguage)) {
				throw new Exception("no source language");
			}

			Language targetLanguage = comboboxTarget.getSelectionModel().getSelectedItem();
			if (Objects.isNull(targetLanguage)) {
				throw new Exception("no target language");
			}

			javafx.application.Platform.runLater(() -> {
				testAreaSource.setText(selected);
				labelStatus.setText(CommonConst.STRING_DOWNLOAD_DATA);
			});

			ListenableFuture<String> task = service.submit(() -> {
				Translate translate = getTranslateWithAuth();
				Translation translation = translate.translate(
						selected,
						TranslateOption.sourceLanguage(sourceLanguage.getCode()),
						TranslateOption.targetLanguage(targetLanguage.getCode()));
				return translation.getTranslatedText();
			});

			task.addListener(() -> {
				try {
					String string = task.get();
					javafx.application.Platform.runLater(() -> {
						testAreaResult.setText(string);
						labelStatus.setText(CommonConst.STRING_DOWNLOAD_DATA_FINISH);
					});

				} catch (Exception e) {
					org.pmw.tinylog.Logger.info(e);
				}
			}, MoreExecutors.directExecutor());
		} catch (Exception ex) {
			org.pmw.tinylog.Logger.info(ex.getMessage());
		}

	}

}
