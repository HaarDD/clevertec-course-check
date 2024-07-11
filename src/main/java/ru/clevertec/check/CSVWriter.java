package main.java.ru.clevertec.check;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import main.java.ru.clevertec.check.CustomExceptions.InternalServerErrorException;

import static main.java.ru.clevertec.check.CheckRunner.CONSOLE_ADDITIONAL_INFO;

public class CSVWriter {

    private static final Double CSV_WRITER_CONSOLE_TAB_WIDTH = 8.0;

    private static final String CSV_WRITER_COLUMN_DELIMITER = ";";
    private static final String CSV_WRITER_LINE_DELIMITER = "\n";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CSVWriterNote {
        CSVWriterFieldTypes type() default CSVWriterFieldTypes.CELL;

        String header() default "";

        String unit() default "";

        String format() default "";
    }

    public enum CSVWriterFieldTypes {
        CELL_CONTAINER, CELL
    }

    public static void writeToCSV(Object object, String filePath) throws InternalServerErrorException {
        try {
            StringBuilder csvContentBuilder = generateCSVContent(object, true, true);
            removeEndEmptyLines(csvContentBuilder);
            String csvContent = csvContentBuilder.toString();
            FileWriterUtils.writeFile(filePath, csvContent);
            if (CONSOLE_ADDITIONAL_INFO) {
                System.out.println("CSV for object: " + object.getClass().getSimpleName());
                System.out.println(formatCSVTable(csvContent));
                System.out.printf("Write object to file complete: %s%nObject class: %s%nLength: %d%n%n",
                        filePath,
                        object.getClass().getSimpleName(),
                        csvContent.length());
            }

        } catch (IllegalAccessException | IOException e) {
            throw new InternalServerErrorException("Unable to write CSV file!");
        }
    }

    private static StringBuilder generateCSVContent(Object object, Boolean appendHeaders, Boolean appendDelimiter) throws IllegalAccessException {
        StringBuilder csvBuilder = new StringBuilder();
        Class<?> topClazz = object.getClass();
        Field[] topFields = topClazz.getDeclaredFields();

        StringJoiner headerJoiner = new StringJoiner(CSV_WRITER_COLUMN_DELIMITER);
        StringJoiner valueJoiner = new StringJoiner(CSV_WRITER_COLUMN_DELIMITER);

        for (Field field : topFields) {
            if (field.isAnnotationPresent(CSVWriterNote.class)) {
                CSVWriterNote annotation = field.getAnnotation(CSVWriterNote.class);
                field.setAccessible(true);
                Object objectValue = field.get(object);

                if (objectValue == null) continue;

                if (Iterable.class.isAssignableFrom(objectValue.getClass())) {
                    boolean isFirstLine = true;
                    for (Object itemIterable : (Iterable<?>) objectValue) {
                        csvBuilder.append(generateCSVContent(itemIterable, isFirstLine, false).append(CSV_WRITER_LINE_DELIMITER));
                        isFirstLine = false;
                    }
                }

                if (annotation.type() == CSVWriterFieldTypes.CELL) {

                    headerJoiner.add(annotation.header());
                    valueJoiner.add(formatValue(objectValue, annotation));

                } else if (annotation.type() == CSVWriterFieldTypes.CELL_CONTAINER) {
                    csvBuilder.append(generateCSVContent(objectValue, true, true).append(CSV_WRITER_LINE_DELIMITER));
                }
            }
        }

        if (appendHeaders && headerJoiner.length() > 0)
            csvBuilder.append(headerJoiner).append(CSV_WRITER_LINE_DELIMITER);
        if (valueJoiner.length() > 0) csvBuilder.append(valueJoiner);
        if (appendDelimiter && !csvBuilder.isEmpty()) csvBuilder.append(CSV_WRITER_LINE_DELIMITER);

        return csvBuilder;
    }

    private static String formatValue(Object object, CSVWriterNote annotation) {
        String formattedValue = object.toString();

        switch (object) {
            case BigDecimal bdValue -> {
                if (!annotation.format().isEmpty()) {
                    DecimalFormat decimalFormat = new DecimalFormat(annotation.format());
                    formattedValue = decimalFormat.format(bdValue);
                }
                if (!annotation.unit().isEmpty()) {
                    formattedValue += annotation.unit();
                }
            }
            case Integer intValue -> {
                if (!annotation.unit().isEmpty()) {
                    formattedValue += annotation.unit();
                }
            }
            case LocalTime localTime -> {
                if (!annotation.format().isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(annotation.format());
                    formattedValue = localTime.format(formatter);
                }
            }
            default -> formattedValue = object.toString();
        }

        return formattedValue;
    }

    private static void removeEndEmptyLines(StringBuilder stringBuilder) {
        while (!stringBuilder.isEmpty() && stringBuilder.charAt(stringBuilder.length() - 1) == CSV_WRITER_LINE_DELIMITER.charAt(0)) {
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
    }

    public static String formatCSVTable(String csvContent) {
        List<List<String>> csvTable = new ArrayList<>();
        String[] lines = csvContent.split(CSV_WRITER_LINE_DELIMITER);

        for (String line : lines) {
            csvTable.add(Arrays.asList(line.split(CSV_WRITER_COLUMN_DELIMITER)));
        }

        Map<Integer, Integer> maxColumnTabulations = calculateColumnWidths(csvTable);

        addTabsToTable(csvTable, maxColumnTabulations);

        int totalTabs = maxColumnTabulations.values().stream().mapToInt(Integer::intValue).sum();

        String tableDelimiter = "-".repeat((int) (totalTabs * CSV_WRITER_CONSOLE_TAB_WIDTH));

        StringBuilder csvTableBuilder = new StringBuilder().append(tableDelimiter).append("\n");

        csvTable.forEach(row -> {
            row.forEach(csvTableBuilder::append);
            csvTableBuilder.append("\n");
        });

        csvTableBuilder.append(tableDelimiter).append("\n");

        return csvTableBuilder.toString();

    }

    private static Map<Integer, Integer> calculateColumnWidths(List<List<String>> table) {
        return table.stream()
                .flatMap(row -> IntStream.range(0, row.size())
                        .mapToObj(colIndex -> {
                            int length = row.get(colIndex).length();
                            int tabulations = (int) Math.ceil((double) length / CSV_WRITER_CONSOLE_TAB_WIDTH) + 1;
                            return Map.entry(colIndex, tabulations);
                        }))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Math::max
                ));
    }

    private static void addTabsToTable(List<List<String>> csvTable, Map<Integer, Integer> maxColumnTabulations) {
        csvTable.forEach(row ->
                IntStream.range(0, row.size()).forEach(i -> {
                    String cell = row.get(i);
                    int cellLength = cell.length();
                    int tabsToAdd = maxColumnTabulations.get(i) - (int) Math.floor(cellLength / CSV_WRITER_CONSOLE_TAB_WIDTH);
                    String tabs = "\t".repeat(tabsToAdd);
                    row.set(i, cell + tabs);
                })
        );
    }

}