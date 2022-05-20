package ru.gb.java.netstorage.common;

import lombok.Getter;
import java.nio.file.Path;

@Getter
public class OpenDirRequest implements BasicRequest {
    private AuthRequest authRequest;
    private String pathStr;

    public OpenDirRequest(AuthRequest authRequest, Path path) {
        this.authRequest = authRequest;
        this.pathStr = path.normalize().toString();
    }

    @Override
    public String getType() {
        return "openIfDirRequest";
    }
}
