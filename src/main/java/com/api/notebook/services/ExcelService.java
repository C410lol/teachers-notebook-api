package com.api.notebook.services;

import com.api.notebook.models.FinalAverageModel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public static @NotNull ByteArrayOutputStream createFinalAverageExcelTable(
            @NotNull List<FinalAverageModel> finalAverageList) {
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            var workBook = new XSSFWorkbook();
            var sheet = workBook.createSheet();
            var rowIndex = 0;

            createSheetHeader(sheet, rowIndex++);

            for (FinalAverageModel finalAverage:
                    finalAverageList) {
                var row = sheet.createRow(rowIndex++);
                createSheetRow(row, 0, finalAverage.getAttendanceNumber());
                createSheetRow(row, 1, finalAverage.getName());
                createSheetRow(row, 2, finalAverage.getAttendanceNumber());
                createSheetRow(row, 3, finalAverage.getFinalGrade());
                createSheetRow(row, 4, finalAverage.getAbsences());
                createSheetRow(row, 5, finalAverage.getCompensatedAbsence());
            }

            workBook.write(byteArrayOutputStream);

            return byteArrayOutputStream;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createSheetHeader(@NotNull XSSFSheet sheet, int index) {
        var row = sheet.createRow(index);
        createSheetRow(row, 0, "Nº CH");
        createSheetRow(row, 1, "Nome");
        createSheetRow(row, 2, "Nº");
        createSheetRow(row, 3, "N");
        createSheetRow(row, 4, "F");
        createSheetRow(row, 5, "AC");
    }

    private static void createSheetRow(@NotNull Row row, int index, String value) {
        row.createCell(index).setCellValue(value);
    }

    private static void createSheetRow(@NotNull Row row, int index, Integer value) {
        row.createCell(index).setCellValue(value);
    }

}
