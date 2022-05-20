package ru.gb.java.netstorage.common;

import lombok.Getter;
import lombok.Setter;

@Getter
public class FilePartResponse implements BasicResponse{
    private String fileName;
    private long fileLength;
    private byte[] partBytes;
    private int partBytesLen;
    @Setter
    private String pathToStr;

    public FilePartResponse(String fileName, long fileLength, byte[] partBytes, int partBytesLen) {
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.partBytes = partBytes;
        this.partBytesLen = partBytesLen;
    }

    @Override
    public String getType() {
        return "filePartResponse";
    }
}
