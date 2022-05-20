package ru.gb.java.netstorage.common;

import lombok.Getter;
import java.nio.file.Path;

@Getter
public class UploadFileRequest implements BasicRequest {
    private AuthRequest authRequest;
    private String pathToUploadFileStr;
    private String pathToLocalFileStr;
    private long fileLength;

    public UploadFileRequest(AuthRequest authRequest, Path pathToUploadFile, Path pathToLocalFile, long fileLength) {
        this.authRequest = authRequest;
        this.pathToUploadFileStr = String.valueOf(pathToUploadFile);
        this.pathToLocalFileStr = String.valueOf(pathToLocalFile);
        this.fileLength = fileLength;
    }

    @Override
    public String getType() {
        return "uploadFileRequest";
    }

}
