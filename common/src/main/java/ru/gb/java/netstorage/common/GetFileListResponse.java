package ru.gb.java.netstorage.common;

import lombok.Getter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class GetFileListResponse implements BasicResponse {
    List<FileInfo> fileListResponse;
    String pathFileListResponse;

    public GetFileListResponse(Path path) {
        try (Stream<Path> list = Files.list(path)) {
            this.pathFileListResponse = path.normalize().toString();
            this.fileListResponse = list.map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getType() {
        return "getFileListResponse";
    }
}
