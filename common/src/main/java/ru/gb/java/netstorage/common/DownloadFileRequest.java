package ru.gb.java.netstorage.common;

import lombok.Getter;
import java.nio.file.Path;

@Getter
public class DownloadFileRequest implements BasicRequest{
    private AuthRequest authRequest;
    private String pathToFileStr;

    public DownloadFileRequest(AuthRequest authRequest, Path pathToFile) {
        this.authRequest = authRequest;
        this.pathToFileStr = pathToFile.normalize().toString();
    }

    @Override
    public String getType() {
        return "downloadFileRequest";
    }
}
