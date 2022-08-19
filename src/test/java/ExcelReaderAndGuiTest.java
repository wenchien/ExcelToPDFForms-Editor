import com.TWReceipt.util.ExcelUtil;
import com.TWReceipt.util.FileUtil;
import com.google.common.io.Files;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.oracle.javafx.scenebuilder.kit.skeleton.SkeletonBuffer;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExcelReaderAndGuiTest {

    private ExcelUtil excelUtil = ExcelUtil.getInstance();
    private FileUtil fileUtil = FileUtil.getInstance();
    private String testFilePath = "src/test/resources/template";
    private String testFile = "purchase_order_test.xlsx";

    private String testFieldMappingPathFile = "src/test/resources/FieldMapping/FieldMapping.fxml";
    private String testFieldMappingPath = "src/test/resources/FieldMapping/";

    /*
    I just create a JFXPanel from Swing EDT before invoking JavaFX Platform.runLater it works.
    I don't know how reliable this solution is, I might choose JFXPanel and JFrame if turns out to be unstable.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testFxom() throws IOException, URISyntaxException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FieldMapping/FieldMapping.fxml"));
        Stage stageLoaded = loader.load();

        // GlueDocument document = new GlueDocument(Files.toString(new File(testFieldMappingPathFile), StandardCharsets.UTF_8));


        //System.out.println(fxomDocument.getGlue().toString());
        //System.out.println(document.toString());
        SkeletonBuffer sb;

    }


    //@Disabled
    @Test
    public void testPdfFuncitonality() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(
                new PdfReader(testFilePath + "\\receipt_template.pdf"),
                new PdfWriter(testFilePath + "\\finished_receipt_template.pdf"));
        PdfAcroForm pdfForm = PdfAcroForm.getAcroForm(pdfDocument, true);
        Map<String, PdfFormField> fields = pdfForm.getFormFields();
        for (String str : fields.keySet()) {
            System.out.println(str);
        }

    }

    @Disabled
    @Test
    public void testExceUtilBackup() {
        assertTrue(fileUtil.createBackupFile(testFilePath, testFile));
    }

    @Disabled
    @Test
    @SuppressWarnings("deprecation")
    public void testExceUtil() throws IOException {
        Files.write(excelUtil.printAsJson(excelUtil.readExcel(testFilePath, testFile, 0, 1)),
                new File(testFilePath + "\\purchase_order_test.json"), StandardCharsets.UTF_8);
        String testJsonData = Files.toString(new File(testFilePath + "\\purchase_order_test.json"), StandardCharsets.UTF_8);
        assertEquals(testJsonData, excelUtil.printAsJson(excelUtil.readExcel(testFilePath, testFile, 0, 1)));
    }

    @Disabled
    @Test
    public void testFunction() throws IOException {

        File file = new File(testFilePath + "\\" + testFile);
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);

        Sheet sheet = workbook.getSheetAt(0);

        for(Row row : sheet) {
            StringBuilder sb = new StringBuilder();
            System.out.println(row.getRowNum() + " - ");

            for (Cell cell : row) {
                sb.append(" : ");
                // System.out.println(cell);
                if (CellType.STRING == cell.getCellType()) {
                    sb.append(cell.getStringCellValue());
                } else if (CellType.NUMERIC == cell.getCellType()) {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        sb.append(cell.getLocalDateTimeCellValue().toLocalDate());
                    } else {
                        sb.append(new BigDecimal(cell.getNumericCellValue()).toPlainString());
                    }
                } else if (CellType.BOOLEAN == cell.getCellType()) {
                    sb.append(cell.getBooleanCellValue());
                }
            }
            System.out.println(sb.toString());
        }
    }

}