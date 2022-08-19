package com.TWReceipt.javafx.controllers;

import com.TWReceipt.main.GuiStarter;
import com.TWReceipt.util.*;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationMainController {

    private static Logger LOGGER = LoggerFactory.getLogger(ApplicationMainController.class);

    private static String mappingFieldFxml = "/FieldMapping/FieldMapping.fxml";

    private ExcelUtil excelUtil = ExcelUtil.getInstance();

    private FileUtil fileUtil = FileUtil.getInstance();

    private PdfUtil pdfUtil = PdfUtil.getInstance();

    private final TypeUtil typeUtil = TypeUtil.getInstance();

    private FileBrowsingSupplier fbSupplier = FileBrowsingSupplier.getInstance();

    private ObservableList<Map<String, String>> observableExcelData;

    private String pdfUrl;

    private File currentFilePreviewed;

    private File currentPdfFilePreviewed;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private MenuItem consoleMenuItem;

    @FXML
    private Button excelFileBrowse;

    @FXML
    private TextField excelFileDir;

    @FXML
    private Button excelFilePreview;

    @FXML
    private TableView<Map<String, String>> excelTablePreview;

    @FXML
    private Button mappingFieldCreate;

    @FXML
    private TextField mappingFieldDir;

    @FXML
    private Button mappingFieldFileBrowse;

    @FXML
    private Button pdfFileBrowse;

    @FXML
    private Button pdfFilePreview;

    @FXML
    private Button mappingFieldFileEdit;

    @FXML
    private TextField pdfFileDir;

    @FXML
    private WebView pdfWebView;

    @FXML
    private void initialize() throws URISyntaxException {
        // Javafx component init
        excelTablePreview.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        WebEngine pdfWebViewEngine = pdfWebView.getEngine();
        pdfWebViewEngine.setJavaScriptEnabled(true);
        pdfUrl = getClass().getResource("/web/viewer.html").toURI().toString();
        pdfWebViewEngine.load(pdfUrl);

        // run every "initFunc" methods to add extra functionality to each Javafx component for this class
        List.of(this.getClass().getDeclaredMethods()).stream().forEach(m -> {
            if (m.getName().startsWith("initFunc")) {
                try {
                    m.invoke(this, null);
                } catch (IllegalAccessException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                } catch (InvocationTargetException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });


    }

    private void initFuncConsoleMenuItem() {
        LOGGER.info("Initializing Application Console menu");
        consoleMenuItem.setOnAction(e -> {
            GuiStarter.stageList.get("consoleLogStage").show();
        });
    }

    private void initFuncAboutMenuItem() {
        LOGGER.info("Initializing Application About menu");
        aboutMenuItem.setOnAction(e -> {
            Alert alertAbout = new Alert(Alert.AlertType.INFORMATION);
            alertAbout.setContentText("");
            alertAbout.show();
        });

    }

    private void initFuncExcelFileBrowse() {
        LOGGER.info("Initializing Application Excel File browsing btn");
        excelFileBrowse.setOnAction(e ->
                fbSupplier.fileBrowsingConsumer(excelFileBrowse, excelFileDir, new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"))
        );
    }

    private void initFuncPdfFileBrowse() {
        LOGGER.info("Initializing Application PDF File browsing btn");
        pdfFileBrowse.setOnAction(e ->
                fbSupplier.fileBrowsingConsumer(pdfFileBrowse, pdfFileDir, new FileChooser.ExtensionFilter("PDF Files", "*.pdf"))
        );
    }

    private void initFuncMappingFieldFileBrowse() {
        LOGGER.info("Initializing Application MappingField File browsing btn");
        mappingFieldFileBrowse.setOnAction(e ->
                fbSupplier.fileBrowsingConsumer(mappingFieldFileBrowse, mappingFieldDir, new FileChooser.ExtensionFilter("JSON Files", "*.json"))
        );
    }

    @SuppressWarnings("deprecation")
    private void initFuncEditExistingMappingFieldFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LOGGER.info("Initializing Application MappingField File editor btn");
        mappingFieldFileEdit.setOnAction(e -> {
            if (!Strings.isNullOrEmpty(mappingFieldDir.getText()) && new File(mappingFieldDir.getText()).exists()) {
                File mappingFieldFile = new File(mappingFieldDir.getText());
                Map<String, String> jsonConfigMap = new HashMap<>();
                Type mapType = typeUtil.getMapType(jsonConfigMap);
                try {
                    jsonConfigMap = gson.fromJson(Files.toString(mappingFieldFile, StandardCharsets.UTF_8), mapType);
                    List<String> pdfFields = jsonConfigMap.keySet().stream().filter(f -> !f.contains(".1") && !f.contains(".2")).collect(Collectors.toList());
                    List<String> excelFields = jsonConfigMap.values().stream().collect(Collectors.toSet()).stream().toList();

                    // Init editor stage
                    initializeMappingFieldWindow(excelFields, pdfFields);

                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
    }

    private void initFuncPdfFilePreview() {
        LOGGER.info("Initializing Application PDF File preview btn");
        pdfFilePreview.setOnAction(e -> {
            if (!Strings.isNullOrEmpty(pdfFileDir.getText()) && new File(pdfFileDir.getText()).exists()) {
                File pdfFile = new File(pdfFileDir.getText());
                if (pdfFile.equals(currentPdfFilePreviewed)) {
                    LOGGER.info("Same PDF file, using current tableview as preview instead");
                    return;
                } else {
                    currentPdfFilePreviewed = pdfFile;
                }

                LOGGER.info("Generating preview table to WebView...");
                try {

                    if (Worker.State.SUCCEEDED == pdfWebView.getEngine().getLoadWorker().getState() || Worker.State.READY == pdfWebView.getEngine().getLoadWorker().getState()) {
                        byte[] pdfData = Files.toByteArray(pdfFile);
                        String base64EncodedPdf = Base64.getEncoder().encodeToString(pdfData);
                        pdfWebView.getEngine().executeScript("openFileFromBase64('" + base64EncodedPdf + "')");
                    }

                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
    }

    private void initFuncExcelFilePreview() {
        LOGGER.info("Initializing Application Excel File preview btn");
        excelFilePreview.setOnAction(e -> {
            if (!Strings.isNullOrEmpty(excelFileDir.getText()) && new File(excelFileDir.getText()).exists()) {
                File excelFile = new File(excelFileDir.getText());
                if (excelFile.equals(currentFilePreviewed)) {
                    LOGGER.info("Same Excel file, using current tableview as preview instead");
                    return;
                } else {
                    currentFilePreviewed = excelFile;
                }

                LOGGER.info("Generating preview table to TableView...");
                List<Map<String, String>> excelData = excelUtil.readExcel(excelFile, 0, 1);
                List<String> excelFields = excelData.get(0).keySet().stream().sorted().collect(Collectors.toList());
                excelFields.stream().forEach(f -> {
                    // Constructor defines the column field name
                    TableColumn<Map<String, String>, String> column = new TableColumn<>(f);
                    column.setCellValueFactory(new MapValueFactory(f));
                    LOGGER.info("Generating TableColumn for Excel field: {}", f);
                    excelTablePreview.getColumns().addAll(column);

                });

                excelTablePreview.setItems(generateObservableListMap(excelData));

            }
        });

    }

    private void initFuncMappingFieldFileCreate() {
        LOGGER.info("Initializing Application Excel File preview btn");
        mappingFieldCreate.setOnAction(e -> {
            if (!Strings.isNullOrEmpty(excelFileDir.getText()) && new File(excelFileDir.getText()).exists()
                    && !Strings.isNullOrEmpty(pdfFileDir.getText()) && new File(pdfFileDir.getText()).exists()) {
                // if both Excel and Pdf exists
                File excelFile = new File(excelFileDir.getText());
                File pdfFile = new File(pdfFileDir.getText());
                List<String> pdfFieldNames = pdfUtil.readPdfFormFields(pdfFile, null).keySet()
                        .stream().filter(f -> !f.contains(".1") && !f.contains(".2")).collect(Collectors.toList());
                List<String> excelFieldNames = excelUtil.readExcel(excelFile, 0, 1).get(0)
                        .keySet().stream().toList();

                // Init editor stage
                try {
                    initializeMappingFieldWindow(excelFieldNames, pdfFieldNames);
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        });
    }

    // Util function
    private ObservableList<Map<String, String>> generateObservableListMap(List<Map<String, String>> excelData) {
        if (observableExcelData != null) {
            return observableExcelData;
        }

        observableExcelData = FXCollections.observableArrayList();
        for (Map<String, String> rowMap : excelData) {
            observableExcelData.add(rowMap);
        }
        return observableExcelData;
    }

    private void initializeMappingFieldWindow(List<String> excelFields, List<String> pdfFields) throws IOException {
        // Init editor stage
        FXMLLoader loader = new FXMLLoader(getClass().getResource(mappingFieldFxml));
        loader.setController(new MappingFieldController(excelFields, pdfFields));
        Stage mappingFieldStage = loader.load();

        mappingFieldStage.initOwner(GuiStarter.stageList.get("applicationMainStage"));
        mappingFieldStage.initModality(Modality.APPLICATION_MODAL);
        mappingFieldStage.showAndWait();
    }

}
