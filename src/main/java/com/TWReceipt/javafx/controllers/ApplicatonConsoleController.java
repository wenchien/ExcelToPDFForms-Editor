package com.TWReceipt.javafx.controllers;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.TWReceipt.logUtil.CustomLoggerAppender;
import com.TWReceipt.util.FileBrowsingSupplier;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Collectors;

public class ApplicatonConsoleController {

    private static Logger LOGGER = LoggerFactory.getLogger(ApplicatonConsoleController.class);

    private FileBrowsingSupplier fbSupplier = FileBrowsingSupplier.getInstance();

    @FXML
    private TextField applicationConsoleDir;

    @FXML
    private ListView<String> applicationConsoleLog;

    @FXML
    private Button applicationConsoleSave;

    @FXML
    private Button consoleApplicationClear;

    @FXML
    private Button applicationConsoleBrowse;

    private ObservableList<String> logAppenderQueue;

    @FXML
    private void initialize() {
        try {
            boolean appenderFound = false;
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
                if (appenderFound) {
                    break;
                }
                for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
                    Appender<ILoggingEvent> appender = index.next();
                    if ("CustomLoggerAppender".equals(appender.getName())) {
                        CustomLoggerAppender customLoggerAppender = (CustomLoggerAppender) appender;
                        logAppenderQueue = customLoggerAppender.getEventQueue();
                        appenderFound = true;
                        LOGGER.info("Custom Appender found...");
                        break;
                    }
                }
            }

            if (logAppenderQueue == null) {
                Alert saveErrorAlert = new Alert(Alert.AlertType.ERROR);
                saveErrorAlert.setContentText("Application has encountered an internal error. " +
                        "Re-installing the application" +
                        " may resolve the problem");
                saveErrorAlert.show();
                return;
            }

            applicationConsoleLog.setItems(logAppenderQueue);

            initConsoleApplicationClearBtn();
            initApplicationConsoleBrowseBtn();
            initConsoleApplicationSaveBtn();

        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("deprecation")
    private void initConsoleApplicationSaveBtn() {
        LOGGER.info("Initializing Application Console Save button");
        applicationConsoleSave.setOnAction(e -> {

            if (!Strings.isNullOrEmpty(applicationConsoleDir.getText())) {
                try {
                    String logFileLocation = applicationConsoleDir.getText() + "\\log.txt";
                    Files.append(logAppenderQueue.stream().collect(Collectors.joining("\n")),
                            new File(logFileLocation),
                            StandardCharsets.UTF_8);

                    Alert saveErrorAlert = new Alert(Alert.AlertType.INFORMATION);
                    saveErrorAlert.setContentText("Saved to " + logFileLocation);
                    saveErrorAlert.show();

                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } else {
                Alert saveErrorAlert = new Alert(Alert.AlertType.ERROR);
                saveErrorAlert.setContentText("You must select a valid directory before saving the log");
                saveErrorAlert.show();
            }
        });
    }

    private void initConsoleApplicationClearBtn() {
        LOGGER.info("Initializing Application Console Clear button");
        consoleApplicationClear.setOnAction(e -> {
            logAppenderQueue.clear();
        });
    }

    private void initApplicationConsoleBrowseBtn() {
        LOGGER.info("Initializing Application Console Browse button");
        applicationConsoleBrowse.setOnAction(e ->
                fbSupplier.directoryBrowsingConsumer(applicationConsoleBrowse, applicationConsoleDir));
    }
}
