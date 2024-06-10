package com.api.notebook.utils;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;

public class ExcelUtils {

    public static void createRowCell(@NotNull Row row, int index, String value) {
        var cell = row.createCell(index);
        cell.setCellValue(value);
    }

    public static void setSheetHeaderRowStyles(@NotNull Workbook workbook, @NotNull Row row) {
        var style = workbook.createCellStyle();
        var fontStyle = workbook.createFont();
        fontStyle.setBold(true);
        style.setFont(fontStyle);
        style.setAlignment(HorizontalAlignment.CENTER);

        for (int x = 0; x < row.getLastCellNum(); x++) {
            if (row.getCell(x) == null) continue;
            row.getCell(x).setCellStyle(style);
        }
    }

    public static void setGlobalCellCentralizedStyle(@NotNull Workbook workbook, @NotNull Sheet sheet, int initIndex) {

        var style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        for (int x = initIndex; x < sheet.getLastRowNum() + 1; x++) {
            if (sheet.getRow(x) == null) continue;

            for (int y = 0; y < sheet.getRow(x).getLastCellNum(); y++) {
                if (sheet.getRow(x).getCell(y) == null) continue;

                sheet.getRow(x).getCell(y).setCellStyle(style);
            }
        }
    }

}
