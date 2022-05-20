package ru.gb.java.netstorage.common;

import lombok.Getter;
import java.nio.file.Path;

@Getter
public class CreateDirRequest implements BasicRequest {
    private AuthRequest authRequest;
    private String pathStr;

    public CreateDirRequest(AuthRequest authRequest, Path path) {
        this.authRequest = authRequest;
        this.pathStr = path.normalize().toString();
    }

    @Override
    public String getType() {
        return "createDirRequest";
    }
}
