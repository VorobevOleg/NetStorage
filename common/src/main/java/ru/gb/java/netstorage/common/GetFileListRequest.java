package ru.gb.java.netstorage.common;

import lombok.Getter;
import java.nio.file.Path;

@Getter
public class GetFileListRequest implements BasicRequest {
    private  AuthRequest authRequest;
    private String pathStr;
    public GetFileListRequest(AuthRequest authRequest, Path path) {
        this.authRequest = authRequest;
        this.pathStr = path.normalize().toString();
    }

    @Override
    public String getType() {
        return "getFileListRequest";
    }
}
