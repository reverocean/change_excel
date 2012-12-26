import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Maps.newHashMap;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ChangeExcel {
    public static void main(String[] args) throws IOException, InvalidFormatException {
        PrintStream out = getPrintStream();
        String excelPath = getExcelPath();
        out.println("开始处理");
        String outPutExcelFileName = "3.xls";
        File inputExcelFile = new File(excelPath + "1.xls");
        if (!inputExcelFile.exists()) {
            inputExcelFile = new File(excelPath + "1.xlsx");
            outPutExcelFileName = "3.xlsx";
        }
        if (!inputExcelFile.exists()) {
            out.println(String.format("请在【%s】目录下放要处理的excel文件，文件名为：1.xls", excelPath));
            return;
        }
        dealWithExcel(out, excelPath, outPutExcelFileName, inputExcelFile);
        out.println("处理完毕...");
    }

    private static void dealWithExcel(PrintStream out, String excelPath, String outPutExcelFileName, File inputExcelFile) throws IOException, InvalidFormatException {
        InputStream inp = new FileInputStream(inputExcelFile);
        Workbook wb = WorkbookFactory.create(inp);
        Map<String, List<String>> dateAndDomains = readDomainsFromSecondExcel(out);

        Sheet sheet = wb.getSheetAt(0);
        String date = null;
        for (Row row : sheet) {
            if (!isNullOrEmpty(getDate(row))) {
                date = getDate(row);
                out.println(String.format("正在处理【%s】的数据", date));
            }
            List<String> domains = dateAndDomains.get(date);

            Cell cell = row.getCell(3);
            if (cell != null) {
                String domain = cell.getStringCellValue().trim();
                if (domains != null && !domains.contains(domain)) {
                    Cell confirmCell = row.getCell(7);
                    if (confirmCell == null) {
                        confirmCell = row.createCell(7);
                    }
                    if (isNullOrEmpty(confirmCell.getStringCellValue())) {
                        confirmCell.setCellType(Cell.CELL_TYPE_STRING);
                        confirmCell.setCellValue("已续费");
                    }
                }
            }
        }
        FileOutputStream fileOut = new FileOutputStream(excelPath + outPutExcelFileName);
        wb.write(fileOut);
        fileOut.close();
    }

    private static String getExcelPath() {
        String excelPath = System.getenv("EXCEL_PATH");
        if (isNullOrEmpty(excelPath)) {
            excelPath = ChangeExcel.class.getResource("/").getPath();
        }
        return excelPath;
    }

    private static PrintStream getPrintStream() {
        PrintStream out = null;
        try {
            out = new PrintStream(System.out, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return out;
    }

    private static Map<String, List<String>> readDomainsFromSecondExcel(PrintStream out) throws IOException, InvalidFormatException {
        Map<String, List<String>> dateAndDomains = newHashMap();
        String excelPath = getExcelPath();
        File secondInputExcelFile = new File(excelPath + "2.xlsx");
        if (!secondInputExcelFile.exists()) {
            secondInputExcelFile = new File(excelPath + "2.xls");
        }

        if (!secondInputExcelFile.exists()) {
            out.println(String.format("请在【%s】目录下放第二个excel文件，文件名为：2.xls", excelPath));
        }
        InputStream inp2 = new FileInputStream(secondInputExcelFile);
        Workbook secondWorkbook = WorkbookFactory.create(inp2);

        Sheet sheet = secondWorkbook.getSheetAt(0);
        String date = null;
        for (Row row : sheet) {
            if (!isNullOrEmpty(getDate(row))) {
                date = getDate(row);
            }
            if (!dateAndDomains.containsKey(date)) {
                dateAndDomains.put(date, new ArrayList<String>());
            }
            List<String> domains = dateAndDomains.get(date);
            Cell cell = row.getCell(3);
            if (cell != null) {
                domains.add(getCellValue(cell));
            }
        }
        return dateAndDomains;
    }

    private static String getDate(Row row) {
        Cell dateCell = row.getCell(0);
        if (dateCell != null) {
            return getCellValue(dateCell);
        }
        return null;
    }

	private static String getCellValue(Cell dateCell) {
		int cellType = dateCell.getCellType();
		switch (cellType) {
		    case Cell.CELL_TYPE_STRING:
		        return dateCell.getStringCellValue();
		    case Cell.CELL_TYPE_NUMERIC:
		        return dateCell.getNumericCellValue() + "";
		}
		return "";
	}
}
