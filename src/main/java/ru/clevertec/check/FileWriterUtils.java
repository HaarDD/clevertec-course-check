package main.java.ru.clevertec.check;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterUtils {

    public static void writeFile(String path, String content) throws IOException {

        try (FileWriter writer = new FileWriter(path, false)) {
            writer.write(content);
        }

    }

}
