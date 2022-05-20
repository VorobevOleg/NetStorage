package ru.gb.java.netstorage.common;

import lombok.Getter;

@Getter
public class DeleteFileFromUploadRequest implements BasicRequest {
    private AuthRequest authRequest;
    private String pathStr;
    private String pathToLocalFileStr;

    public DeleteFileFromUploadRequest(AuthRequest authRequest, String path, String pathToLocalFileStr) {
        this.authRequest = authRequest;
        this.pathStr = path;
        this.pathToLocalFileStr = pathToLocalFileStr;
    }

    @Override
    public String getType() {
        return "deleteFileFromUploadRequest";
    }
}
