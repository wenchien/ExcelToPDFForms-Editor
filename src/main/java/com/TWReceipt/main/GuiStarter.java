package com.TWReceipt.main;

import com.TWReceipt.javafx.controllers.ApplicationMainController;
import com.TWReceipt.javafx.controllers.ApplicatonConsoleController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

;

public class GuiStarter extends Application {

    private static Logger LOGGER = LoggerFactory.getLogger(GuiStarter.class);

    private static String consoleLogFxml =  "/FieldMapping/ApplicationConsole.fxml";

    private static String applicationMainFxml = "/FieldMapping/ApplicationMain.fxml";

    private static String applicationIcon = "/icon/mappfieldEditor.png";

    public static Map<String, Stage> stageList = new HashMap<>();

    private Stage applicationConsoleLog;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            initFuncApplicationConsoleLog();
            if (applicationConsoleLog == null) {
                throw new IllegalStateException("ApplicationConsoleLog not initialized");
            }
            stageList.put("consoleLogStage", applicationConsoleLog);

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(applicationMainFxml));
        loader.setController(new ApplicationMainController());

        Stage root = loader.load();
        root.getIcons().add(new Image(GuiStarter.class.getResourceAsStream(applicationIcon)));
        stageList.put("applicationMainStage", root);
        root.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void initFuncApplicationConsoleLog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(consoleLogFxml));
        loader.setController(new ApplicatonConsoleController());
        applicationConsoleLog = loader.<Stage>load();
    }
}
