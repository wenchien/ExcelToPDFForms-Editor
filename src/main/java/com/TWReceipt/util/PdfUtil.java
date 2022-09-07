package com.TWReceipt.util;

import com.google.common.base.Strings;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class PdfUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(PdfUtil.class);

    private static final PdfUtil INSTANCE = new PdfUtil();

    private static final String COMPLETED_PREFIX = "completed_";

    private PdfUtil() {

    }

    public static PdfUtil getInstance() {
        return INSTANCE;
    }

    public Map<String, PdfFormField> readPdfFormFields(String pdfSourceFilePath, String pdfSourceFile, String pdfDestination) {
        return readPdfFormFields(new File(pdfSourceFilePath + "\\" + pdfSourceFile), Strings.isNullOrEmpty(pdfDestination) ?
                new File(pdfSourceFilePath + "\\" +COMPLETED_PREFIX + pdfSourceFile) : new File(pdfDestination + "\\" + COMPLETED_PREFIX  + pdfSourceFile));
    }

    public Map<String, PdfFormField> readPdfFormFields(File pdfSourceFile, File pdfDestinationFile) {
        try {
            PdfDocument pdfDocument = new PdfDocument(
                    new PdfReader(pdfSourceFile));
            PdfAcroForm pdfForm = PdfAcroForm.getAcroForm(pdfDocument, true);
            return pdfForm.getFormFields();
        } catch(IOException ex) {
            // change all these to logger
            ex.printStackTrace();
        }
        return Collections.emptyMap();
    }

    public void writeToPdf(File excelFile, File templatePdfFile, List<String> pdfFieldNames,
                           Map<String, String> jsonConfigMap, Map<String, String> data) throws IOException {
        Path templatedPdfPath = Path.of(templatePdfFile.toURI());
        Path excelFilePath = Path.of(excelFile.toURI());
        Path templatedPdfDirectory = templatedPdfPath.toAbsolutePath().getParent();

        String[] splitFileName = excelFilePath.getFileName().toString().split(".");

        String copiedTemplatedPdfPath = templatedPdfDirectory + splitFileName[0]
                + "_" + System.currentTimeMillis() + "." + splitFileName[1];

        Path copiedFile = Files.copy(Path.of(templatePdfFile.toURI()), Path.of(copiedTemplatedPdfPath), StandardCopyOption.REPLACE_EXISTING);
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(copiedFile.toFile()));
        PdfAcroForm pdfForm = PdfAcroForm.getAcroForm(pdfDocument, true);

        for (String pdfField : pdfFieldNames) {
            // Each field is a key
            String excelField = jsonConfigMap.get(pdfField);
            String excelValue = data.get(excelField);
            pdfForm.getField(pdfField).setValue(excelValue);
        }

        pdfDocument.close();
    }

}
