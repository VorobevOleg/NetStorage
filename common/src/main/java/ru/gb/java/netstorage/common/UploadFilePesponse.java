package ru.gb.java.netstorage.common;

import lombok.Getter;

@Getter
public class UploadFilePesponse implements BasicResponse {
    private String pathToLocalFileStr;
    private String pathToUploadFileStr;
    private boolean needDeleteFile;
    private boolean noFreeStorage;

    public UploadFilePesponse(String pathToLocalFile, String pathToUploadFileStr, boolean needDeleteFile, boolean noFreeStorage) {
        this.pathToLocalFileStr = pathToLocalFile;
        this.pathToUploadFileStr = pathToUploadFileStr;
        this.needDeleteFile = needDeleteFile;
        this.noFreeStorage = noFreeStorage;
    }

    @Override
    public String getType() {
        return "uploadFileResponse";
    }
}
