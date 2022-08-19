package com.TWReceipt.util;

import com.google.common.base.Strings;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class PdfUtil {
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
                    new PdfReader(pdfSourceFile),
                    new PdfWriter(pdfDestinationFile));
            PdfAcroForm pdfForm = PdfAcroForm.getAcroForm(pdfDocument, true);
            return pdfForm.getFormFields();
        } catch(IOException ex) {
            // change all these to logger
            ex.printStackTrace();
        }
        return Collections.emptyMap();
    }

}
