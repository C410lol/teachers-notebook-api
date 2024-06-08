package com.api.notebook.utils;

import com.api.notebook.models.entities.FinishedNotebookEntity;
import com.api.notebook.models.entities.FinishedStudentEntity;
import com.api.notebook.models.entities.StudentEntity;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.List;

public class FinishedNotebookUtils {

    public static @NotNull ByteArrayResource buildFinishedNotebooksWorkbook(
            String classe,
            String bimester,
            @NotNull List<FinishedNotebookEntity> finishedNotebooks,
            List<StudentEntity> students
    ) throws IOException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Notas " + bimester + " Bim");

        createStudentRows(
                sheet,
                students
        );

        var currentCellNumber = 2;
        for (FinishedNotebookEntity finishedNotebook:
                finishedNotebooks) {
            createFinishedNotebookContainer(
                    sheet,
                    currentCellNumber,
                    finishedNotebook,
                    classe,
                    bimester
            );
            currentCellNumber += 5;
        }

        sheet.addMergedRegion(new CellRangeAddress(
                0, 0,
                2, sheet.getLastRowNum()
        ));
        sheet.addMergedRegion(new CellRangeAddress(
                0, 6,
                0, 1
        ));

        ExcelUtils.setGlobalCellCentralizedStyle(workbook, sheet, 1);
        ExcelUtils.setSheetHeaderRowStyles(workbook, sheet.getRow(1));
        ExcelUtils.setSheetHeaderRowStyles(workbook, sheet.getRow(3));
        ExcelUtils.setSheetHeaderRowStyles(workbook, sheet.getRow(5));
        ExcelUtils.setSheetHeaderRowStyles(workbook, sheet.getRow(7));

        workbook.write(byteArrayOutputStream);
        return new ByteArrayResource(byteArrayOutputStream.toByteArray());
    }




    private static void createStudentRows(
            @NotNull Sheet sheet,
            @NotNull List<StudentEntity> students
    ) {
        var rowCount = 7;

        //Create students header
        var headerRow = sheet.createRow(rowCount++);
        ExcelUtils.createRowCell(headerRow, 0, "Nº CH.");
        ExcelUtils.createRowCell(headerRow, 1, "NOMES");
        sheet.setColumnWidth(0, (int) (6 * 1.5 * 256));
        sheet.setColumnWidth(1, (int) (33 * 1.5 * 256));

        //Create student rows
        for (StudentEntity student:
                students) {
            var row = sheet.createRow(rowCount++);
            ExcelUtils.createRowCell(row, 0, student.getNumber().toString());
            ExcelUtils.createRowCell(row, 1, student.getName());
        }
    }




    private static void createFinishedNotebookContainer(
            Sheet sheet,
            int cellNumber,
            FinishedNotebookEntity finishedNotebook,
            String classe,
            String bimester
    ) {
        //Create year and bimester values
        createYearAndBimesterRows(sheet, cellNumber, bimester);

        //Create classe values
        createClasseRows(sheet, cellNumber, classe);

        //Create subject values
        createSubjectValues(sheet, cellNumber, finishedNotebook.getNotebook().getSubject().name());

        //Create students header
        createStudentsHeader(sheet, cellNumber);

        //Create students values
        var studentRow = 8;
        for (FinishedStudentEntity finishedStudent:
                finishedNotebook.getFinishedStudents()) {
            createFinishedStudentValues(
                    sheet,
                    studentRow,
                    cellNumber,
                    finishedStudent.getStudent().getNumber(),
                    finishedStudent.getFinalGrade(),
                    finishedStudent.getAbsences()
            );
            studentRow++;
        }
    }

    private static void createYearAndBimesterRows(
            @NotNull Sheet sheet,
            int cellNumber,
            String bimester
    ) {
        //Create header
        Row rowHeader = sheet.getRow(1);
        if (rowHeader == null) rowHeader = sheet.createRow(1);

        ExcelUtils.createRowCell(rowHeader, cellNumber, "ANO");
        ExcelUtils.createRowCell(rowHeader, cellNumber + 2, "BIM.");

        sheet.addMergedRegion(new CellRangeAddress(
                rowHeader.getRowNum(), rowHeader.getRowNum(),
                cellNumber, cellNumber + 1));
        sheet.addMergedRegion(new CellRangeAddress(
                rowHeader.getRowNum(), rowHeader.getRowNum(),
                cellNumber  + 2, cellNumber + 3));


        //Create values
        Row rowValues = sheet.getRow(2);
        if (rowValues == null) rowValues = sheet.createRow(2);

        ExcelUtils.createRowCell(rowValues, cellNumber, String.valueOf(Year.now().getValue()));
        ExcelUtils.createRowCell(rowValues, cellNumber + 2, bimester.toUpperCase());

        sheet.addMergedRegion(new CellRangeAddress(
                rowValues.getRowNum(), rowValues.getRowNum(),
                cellNumber, cellNumber + 1));
        sheet.addMergedRegion(new CellRangeAddress(
                rowValues.getRowNum(), rowValues.getRowNum(),
                cellNumber  + 2, cellNumber + 3));
    }

    private static void createClasseRows(
            @NotNull Sheet sheet,
            int cellNumber,
            String classe
    ) {
        var rowHeader = sheet.getRow(3);
        if (rowHeader == null) rowHeader = sheet.createRow(3);

        ExcelUtils.createRowCell(rowHeader, cellNumber, "CLASSE");

        sheet.addMergedRegion(new CellRangeAddress(
                rowHeader.getRowNum(), rowHeader.getRowNum(),
                cellNumber, cellNumber + 3
        ));


        //Create values
        var rowValues = sheet.getRow(4);
        if (rowValues == null) rowValues = sheet.createRow(4);

        ExcelUtils.createRowCell(rowValues, cellNumber, classe.replaceAll("_", " ").toUpperCase());

        sheet.addMergedRegion(new CellRangeAddress(
                rowValues.getRowNum(), rowValues.getRowNum(),
                cellNumber, cellNumber + 3
        ));
    }

    private static void createSubjectValues(
            @NotNull Sheet sheet,
            int cellNumber,
            String subject
    ) {
        var rowHeader = sheet.getRow(5);
        if (rowHeader == null) rowHeader = sheet.createRow(5);

        ExcelUtils.createRowCell(rowHeader, cellNumber, "COMP.CUR.");

        sheet.addMergedRegion(new CellRangeAddress(
                rowHeader.getRowNum(), rowHeader.getRowNum(),
                cellNumber, cellNumber + 3
        ));


        //Create values
        var rowValues = sheet.getRow(6);
        if (rowValues == null) rowValues = sheet.createRow(6);

        ExcelUtils.createRowCell(rowValues, cellNumber, subject.replaceAll("_", " ").toUpperCase());

        sheet.addMergedRegion(new CellRangeAddress(
                rowValues.getRowNum(), rowValues.getRowNum(),
                cellNumber, cellNumber + 3
        ));
    }

    private static void createStudentsHeader(
            @NotNull Sheet sheet,
            int cellNumber
    ) {
        var rowHeader = sheet.getRow(7);
        if (rowHeader == null) rowHeader = sheet.createRow(7);

        ExcelUtils.createRowCell(rowHeader, cellNumber, "Nº");
        ExcelUtils.createRowCell(rowHeader, cellNumber + 1, "N");
        ExcelUtils.createRowCell(rowHeader, cellNumber + 2, "F");
        ExcelUtils.createRowCell(rowHeader, cellNumber + 3, "AC");
    }

    private static void createFinishedStudentValues(
            @NotNull Sheet sheet,
            int rowNumber,
            int cellNumber,
            int studentNumber,
            double grade,
            int absences
    ) {
        var row = sheet.getRow(rowNumber);
        if (row == null) row = sheet.createRow(rowNumber);

        ExcelUtils.createRowCell(row, cellNumber, String.valueOf(studentNumber));
        ExcelUtils.createRowCell(row, cellNumber + 1, String.valueOf(grade).replace(".", ","));
        ExcelUtils.createRowCell(row, cellNumber + 2, String.valueOf(absences));
        ExcelUtils.createRowCell(row, cellNumber + 3, "-");
    }

}
