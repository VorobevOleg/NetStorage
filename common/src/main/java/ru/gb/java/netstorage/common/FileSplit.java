package ru.gb.java.netstorage.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class FileSplit {
    private static final int MB_1 = 1_000_000;

    public void split(Path path, BiConsumer<byte[], Integer> filePartConsumer) {
        byte[] filePart = new byte[MB_1];
        int len = 0;
        try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
            while ((len = fileInputStream.read(filePart)) != -1) {
                filePartConsumer.accept(filePart, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
