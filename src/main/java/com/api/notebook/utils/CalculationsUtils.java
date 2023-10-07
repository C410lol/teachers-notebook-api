package com.api.notebook.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

public class CalculationsUtils {

    public static @NotNull Double getFirstDecimalHouseDoubleAverage(@NotNull List<Double> numbers) {
        double allNumbersSum = sumEveryItemOfNumberList(numbers);
        var approximatedDecimal = new DecimalFormat("#.#");
        return Double.parseDouble(approximatedDecimal.format(allNumbersSum / numbers.size()));
    }

    public static Double sumEveryItemOfNumberList(@NotNull List<Double> numbers) {
        double allNumbersSum = 0;
        for (Double currentNumber:
                numbers) {
            allNumbersSum += currentNumber;
        }
        return allNumbersSum;
    }

}
