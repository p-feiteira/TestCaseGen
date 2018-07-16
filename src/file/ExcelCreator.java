package file;

import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.FileHandling;
import utils.ObjectParse.Diagram;
import utils.ObjectParse.MultipleDiagrams;
import utils.ObjectParse.SingleDiagram;
import utils.ObjectParse.Steps;

import java.io.FileOutputStream;

public class ExcelCreator {

    private static final String TEST_CONST = "Test Cases";
    private static final String INITIAL_NODE = "init";
    private static final String END_NODE = "end";
    private static final String ASSUMPTIONS_AND_CONDITIONS = "Assumptions & Conditions:";
    private static final String TEST_NAME = "TestCases.xlsx";

    private static String[] columns = {"Name", "Description", "Step", "Step name", "Expected result"};

    public static String createExcel(byte[] data) {
        MultipleDiagrams result = null;
        SingleDiagram diagram = null;
        String json = processJSON(getBody(data));
        Gson g = new Gson();
        try {
            result = g.fromJson(json, MultipleDiagrams.class);
            return writeExcel(result);
        } catch (Exception e) {
            diagram = g.fromJson(json, SingleDiagram.class);
            return writeExcel(diagram);
        }

    }

    private static String getBody(byte[] data) {
        String[] split = new String(data).split("\n");
        return split[4];
    }

    private static String writeExcel(Object result) {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet(TEST_CONST);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
        int firstRow = 1;
        int lastRow = 1;
        if(result instanceof SingleDiagram){
            SingleDiagram sd = (SingleDiagram) result;
            Diagram d = sd.getDiagram();
            String[] split = d.getDesc().split(":");
            int stepCount = 0;
            for (Steps step : d.getNodes()) {
                Row row = sheet.createRow(lastRow++);
                row.createCell(2).setCellValue(stepCount++);
                if (step.getName().toLowerCase().equals(INITIAL_NODE)) {
                    row.createCell(3).setCellValue(ASSUMPTIONS_AND_CONDITIONS);
                    row.createCell(4).setCellValue(split[1]);
                } else {
                    row.createCell(3).setCellValue(step.getName().toLowerCase());
                    if (step.getName().toLowerCase().equals(END_NODE)) {
                        row.createCell(4).setCellValue(END_NODE);
                    } else {
                        row.createCell(4).setCellValue("Success");
                    }
                }
            }
            mergeCells(sheet,firstRow,lastRow,d.getName(),split[0]);
        }else{
            MultipleDiagrams diagrams = (MultipleDiagrams) result;
            for (Diagram d : diagrams.getDiagrams()) {
                String[] split = d.getDesc().split(":");
                int stepCount = 0;
                for (Steps step : d.getNodes()) {
                    Row row = sheet.createRow(lastRow++);
                    row.createCell(2).setCellValue(stepCount++);
                    if (step.getName().toLowerCase().equals(INITIAL_NODE)) {
                        row.createCell(3).setCellValue(ASSUMPTIONS_AND_CONDITIONS);
                        row.createCell(4).setCellValue(split[1]);
                    } else {
                        row.createCell(3).setCellValue(step.getName().toLowerCase());
                        if (step.getName().toLowerCase().equals(END_NODE)) {
                            row.createCell(4).setCellValue(END_NODE);
                        } else {
                            row.createCell(4).setCellValue("Success");
                        }
                    }
                }

                mergeCells(sheet,firstRow,lastRow,d.getName(),split[0]);
                firstRow = lastRow;
            }
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        String path = FileHandling.BASE_PATH + TEST_NAME;
        try {

            FileOutputStream fileOutputStream = new FileOutputStream(path);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;

    }

    private static void mergeCells(Sheet sheet,int firstRow, int lastRow, String name, String desc){
        Row row = sheet.getRow(firstRow);
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow - 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow - 1, 1, 1));
        row.createCell(0).setCellValue(name);
        row.createCell(1).setCellValue(desc);
    }


    private static String processJSON(String json) {
        json = json.replace("\\", "");
        json = json.replace("\"{", "{");
        json = json.replace("}\"", "}");
        return json;
    }


}
