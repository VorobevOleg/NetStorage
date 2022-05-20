package ru.gb.java.netstorage.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse implements BasicResponse {
    private boolean authOk;
    private int maxFolderDepth;

    public AuthResponse(boolean authOk) {
        this.authOk = authOk;
    }

    @Override
    public String getType () { return "authResponse"; }
}
