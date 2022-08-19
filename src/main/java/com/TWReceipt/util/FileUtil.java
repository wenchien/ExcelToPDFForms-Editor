package com.TWReceipt.util;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class FileUtil {

    public static final FileUtil INSTANCE = new FileUtil();

    private TypeUtil typeUtil = TypeUtil.getInstance();

    private FileUtil () {

    }

    public static FileUtil getInstance() {
        return INSTANCE;
    }

    public boolean createBackupFile(String excelSourceFilePath, String excelFile) {
        return createBackupFile(excelSourceFilePath,excelFile, null);
    }

    public boolean createBackupFile(String excelSourceFilePath, String excelFile, String destination) {
        Path source = Paths.get(excelSourceFilePath + "\\" + excelFile);
        Path target = Strings.isNullOrEmpty(destination) ?
                Paths.get(excelSourceFilePath + "\\backup_" + excelFile) : Paths.get(destination + "\\backup_" + excelFile);
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch(IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @SuppressWarnings("deprecated")
    public boolean createTemplateConfigFile(Map<String, String> excelFieldToPdfFieldMap, String destination) {
        //Path target = Strings.isNullOrEmpty(destination) ? Paths.get() : Paths.get();
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type mapType = typeUtil.getMapType(excelFieldToPdfFieldMap);
            com.google.common.io.Files.write(gson.toJson(excelFieldToPdfFieldMap, mapType), new File(destination), StandardCharsets.UTF_8);
            return true;
        } catch(Exception e) {

        }
        return false;
    }

}
