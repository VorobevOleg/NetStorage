package ru.gb.java.netstorage.common;

import lombok.Getter;

@Getter
public class CreateDirResponse implements BasicResponse{
    private GetFileListResponse getFileListResponse;
    private boolean createDirOk;

    public CreateDirResponse(GetFileListResponse getFileListResponse, boolean createDirOk) {
        this.getFileListResponse = getFileListResponse;
        this.createDirOk = createDirOk;
    }

    @Override
    public String getType() {
        return "createDirResponse";
    }
}
