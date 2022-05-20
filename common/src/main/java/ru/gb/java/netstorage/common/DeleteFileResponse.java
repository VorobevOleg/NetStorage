package ru.gb.java.netstorage.common;

import lombok.Getter;

@Getter
public class DeleteFileResponse implements BasicResponse{
    private GetFileListResponse getFileListResponse;
    private boolean deleteFileOk;

    public DeleteFileResponse(GetFileListResponse getFileListResponse, boolean deleteFileOk) {
        this.getFileListResponse = getFileListResponse;
        this.deleteFileOk = deleteFileOk;
    }

    @Override
    public String getType() {
        return "deleteFileResponse";
    }
}
