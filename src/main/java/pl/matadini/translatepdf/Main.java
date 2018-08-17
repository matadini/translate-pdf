package pl.matadini.translatepdf;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.matadini.translatepdf.gui.common.CommonUtil;
import pl.matadini.translatepdf.gui.main.MainPaneController;

public class Main extends Application {

	private static Stage stage;

	public static Stage getPrimaryStageInstance() {
		return stage;
	}

	public static void main(String[] args) {
		launch(args);
	}

	private MainPaneController controller;

	@Override	
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		controller = CommonUtil.loadFxml(new MainPaneController(), MainPaneController.class.getResource("MainPane.fxml"));

		Scene value = new Scene(controller);
		value.setOnKeyPressed(event -> {
			controller.translate();
		});
		controller.bindScene(value);

		primaryStage.setScene(value);
		primaryStage.setWidth(1024);
		primaryStage.setHeight(768);
		primaryStage.centerOnScreen();
		primaryStage.show();
	}

}
