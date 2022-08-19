package com.TWReceipt.javafx.controllers;

import com.TWReceipt.util.TypeUtil;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class MappingFieldController {

    private static Logger LOGGER = LoggerFactory.getLogger(MappingFieldController.class);

    @FXML
    private ScrollPane fieldMappingScrollPane;

    private GridPane fieldMappingGridPane;

    private Stage fieldMappingStage;

    private Map<String, TextField> excelColumnToFieldMap = new HashMap<>();

    private Map<String, TextField> pdfColumnToFieldMap = new HashMap<>();

    private final TypeUtil typeUtil = TypeUtil.getInstance();

    private List<String> excelFieldNames;

    private List<String> pdfFieldNames;

    private List<ComboBox> excelTextFieldComponents = new ArrayList<>();

    private List<TextField> pdfTextFieldComponents = new ArrayList<>();

    private static final int JAVAFX_MAPPINGFIELD_COMPONENT_GAP = 10;

    private static int excelColumnBaseIndex = 0;

    private static int pdfColumnBaseIndex = 1;

    public MappingFieldController() {   }

    public MappingFieldController(List<String> excelFieldNames, List<String> pdfFieldNames) {
        this.excelFieldNames = excelFieldNames;
        this.pdfFieldNames = pdfFieldNames;


        // init gridpane, the dynamic section
        Supplier<ColumnConstraints> columnConstraintsSupplier = this::columnConstraintGenerator;

        // create new constraints for row and set their percentage
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.NEVER);

        fieldMappingGridPane = new GridPane();
        fieldMappingGridPane.getRowConstraints().add(rowConstraints);
        fieldMappingGridPane.setHgap(JAVAFX_MAPPINGFIELD_COMPONENT_GAP);
        fieldMappingGridPane.setVgap(JAVAFX_MAPPINGFIELD_COMPONENT_GAP);

        fieldMappingGridPane.getColumnConstraints().add(columnConstraintsSupplier.get());
        fieldMappingGridPane.getColumnConstraints().add(columnConstraintsSupplier.get());
    }

    // flow is construction -> fxml injection -> initialization
    @FXML
    private void initialize() {
        try {
            Random rand = new Random(System.currentTimeMillis());

            buildExcelFieldNames(excelFieldNames, pdfFieldNames.size());
            buildPdfFieldNames(rand, pdfFieldNames, pdfTextFieldComponents);

            fieldMappingGridPane.add(generateConfirmButton(), pdfColumnBaseIndex, 0);

            fieldMappingScrollPane.setContent(fieldMappingGridPane);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void buildExcelFieldNames(List<String> fieldNames, int pdfNumOfFields) {
        LOGGER.info("building Excel Field Names for the Field Mapper window...");
        generateMappingFieldComboBox(fieldNames, pdfNumOfFields);
    }

    private void buildPdfFieldNames(Random rand, List<String> fieldNames, List<TextField> fieldList) {
        LOGGER.info("building PDF Field Names for the Field Mapper window...");
        generateTextField(rand, fieldNames, fieldList);
    }

    private void generateTextField(Random rand, List<String> fieldNames, List<TextField> fieldList) {
        int rowIndex = 1;
        for(String fieldName : fieldNames) {
            String id = fieldName + rand.nextDouble();
            TextField textField = new TextField();
            textField.setPromptText(fieldName);
            fieldList.add(textField);
            fieldMappingGridPane.add(textField, pdfColumnBaseIndex, rowIndex++);
            // System.out.println("Row Index: " + rowIndex);
        }

    }

    private void generateMappingFieldComboBox(List<String> fieldNames, int pdfNumOfFields) {
        int rowIndex = 1;
        ObservableList comboItems = FXCollections.observableList(fieldNames);
        for(int i = 0; i < pdfNumOfFields; ++i) {

            ComboBox comboBox = new ComboBox();
            comboBox.setPromptText("Select a value");
            comboBox.setItems(comboItems);
            comboBox.setMaxWidth(200.0);

            excelTextFieldComponents.add(comboBox);
            fieldMappingGridPane.add(comboBox, excelColumnBaseIndex, rowIndex++);
        }
    }


    private ColumnConstraints columnConstraintGenerator() {

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(Priority.NEVER);
        columnConstraints.setPercentWidth(100.00);
        columnConstraints.setMinWidth(10.0);
        columnConstraints.setPrefWidth(100.0);

        return columnConstraints;
    }

    @SuppressWarnings("deprecation")
    private Button generateConfirmButton() {

        Button confirmButton = new Button();
        confirmButton.prefWidth(100.0);
        confirmButton.setMinWidth(20.0);
        confirmButton.setId("mappingFieldConfirmButton");
        confirmButton.setText("Done");
        confirmButton.setOnAction(e ->
            {
                String jsonConfig = writeMappingFieldToJson();
                try {

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save");
                    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                    File selectedFile = fileChooser.showSaveDialog((Stage)fieldMappingScrollPane.getScene().getWindow());
                    if (selectedFile == null) {
                        throw new NullPointerException();
                    }
                    Files.write(jsonConfig, selectedFile, StandardCharsets.UTF_8);
                    LOGGER.info("Written the following to config.json: \n" + jsonConfig);
                } catch (NullPointerException | IOException ex) {
                    LOGGER.error("Event: " + e.getSource() + " : " + ex.getMessage(), ex);
                    closeMappingFieldWindow(true);
                }

                closeMappingFieldWindow(false);
            });

        return confirmButton;
    }

    private String writeMappingFieldToJson() {
        Map<String, String> jsonConfigMap = new HashMap<>();
        for (int i = 0 ; i < pdfTextFieldComponents.size(); ++i) {
            String finalpdfFieldString = Strings.isNullOrEmpty(pdfTextFieldComponents.get(i).getText())
                    ? pdfTextFieldComponents.get(i).getPromptText() : pdfTextFieldComponents.get(i).getText();
            String finalexcelFieldString = excelTextFieldComponents.get(i).<String>getValue() == null
                    ? "undefined" : (String)excelTextFieldComponents.get(i).<String>getValue();
            jsonConfigMap.put(finalpdfFieldString, finalexcelFieldString);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type mapType = typeUtil.getMapType(jsonConfigMap);

        return gson.toJson(jsonConfigMap, mapType);
    }

    private void closeMappingFieldWindow(boolean isError) {
        fieldMappingStage = (Stage)fieldMappingScrollPane.getScene().getWindow(); // used for closing field mapping window
        fieldMappingStage.close();
    }
}
