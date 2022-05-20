package ru.gb.java.netstorage.common;

import lombok.Getter;

@Getter
public class DeleteFileFromUploadResponse implements BasicResponse{
    private GetFileListResponse getFileListResponse;
    private boolean deleteFileOk;
    private String pathToLocalFileStr;
    private String pathToUploadFileStr;

    public DeleteFileFromUploadResponse(GetFileListResponse getFileListResponse, boolean deleteFileOk,
                                        String pathToLocalFileStr, String pathToUploadFileStr) {
        this.getFileListResponse = getFileListResponse;
        this.deleteFileOk = deleteFileOk;
        this.pathToLocalFileStr = pathToLocalFileStr;
        this.pathToUploadFileStr = pathToUploadFileStr;
    }

    @Override
    public String getType() {
        return "deleteFileFromUploadResponse";
    }
}
