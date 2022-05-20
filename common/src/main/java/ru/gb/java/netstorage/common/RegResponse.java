package ru.gb.java.netstorage.common;

import lombok.Getter;
import lombok.Setter;

@Getter
public class RegResponse implements BasicResponse {
    boolean regOk;
    @Setter
    private int maxFolderDepth;

    public RegResponse(boolean regOk) {
        this.regOk = regOk;
    }

    @Override
    public String getType () { return "regResponse"; }
}
