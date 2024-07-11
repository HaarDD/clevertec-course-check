package main.java.ru.clevertec.check;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Map;

import main.java.ru.clevertec.check.CustomExceptions.BadRequestException;

import static main.java.ru.clevertec.check.CheckRunner.CONSOLE_ADDITIONAL_INFO;

public class CSVReader {

    private final static String CSV_READER_COLUMN_SEPARATOR = ";";

    public static <T extends IdentifiableObject> Map<Integer, T> readCSVtoMap(String filePath, Class<T> clazz) throws IOException {
        Map<Integer, T> objects = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(CSV_READER_COLUMN_SEPARATOR);
                T object = createItem(values, clazz);
                objects.put(object.getId(), object);
            }
        }

        if (CONSOLE_ADDITIONAL_INFO) {
            System.out.printf("Load objects map from csv: %s complete.%n" +
                            "Objects count: %d%n" +
                            "Objects class: %s%n%n",
                    filePath,
                    objects.size(),
                    clazz.getSimpleName());
        }

        return objects;
    }

    private static <T> T createItem(String[] values, Class<T> clazz) {
        try {
            @SuppressWarnings("unchecked") Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructors()[0];
            Object[] params = new Object[values.length];
            Field[] fields = clazz.getDeclaredFields();

            for (int i = 0; i < values.length; i++) {
                Field field = fields[i];
                Class<?> fieldType = field.getType();
                CSVReaderNote note = field.getAnnotation(CSVReaderNote.class);

                params[i] = parseValue(values[i], fieldType, note, field.getName());
            }

            return constructor.newInstance(params);

        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new BadRequestException("Failed to create item instance");
        }
    }

    private static Object parseValue(String value, Class<?> fieldType, CSVReaderNote note, String fieldName) {
        if (fieldType.equals(Integer.class)) {
            Integer intValue = Integer.parseInt(value);
            validateInteger(intValue, note, fieldName);
            return intValue;
        } else if (fieldType.equals(BigDecimal.class)) {
            BigDecimal bigDecimalValue = new BigDecimal(value.replace(",", "."));
            validateBigDecimal(bigDecimalValue, note, fieldName);
            return bigDecimalValue;
        } else if (fieldType.equals(Boolean.class) && note != null) {
            return value.equals(note.trueValue());
        } else {
            if (note != null && note.notEmpty() && (value == null || value.isEmpty())) {
                throw new BadRequestException("Field " + fieldName + " cannot be null or empty");
            }
            return value;
        }
    }

    private static void validateInteger(Integer value, CSVReaderNote note, String fieldName) {
        if (note != null) {
            if (note.minValue() != Integer.MIN_VALUE && value < note.minValue()) {
                throw new BadRequestException("Value for field " + fieldName + " is less than minValue");
            }
            if (note.positive() && value <= 0) {
                throw new BadRequestException("Value for field " + fieldName + " must be positive");
            }
        }
    }

    private static void validateBigDecimal(BigDecimal value, CSVReaderNote note, String fieldName) {
        if (note != null) {
            if (note.minValue() != Integer.MIN_VALUE && value.intValue() < note.minValue()) {
                throw new BadRequestException("Value for field " + fieldName + " is less than minValue");
            }
            if (note.positive() && value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Value for field " + fieldName + " must be positive");
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CSVReaderNote {
        String trueValue() default "";

        int minValue() default Integer.MIN_VALUE;

        boolean notEmpty() default false;

        boolean positive() default false;
    }
}