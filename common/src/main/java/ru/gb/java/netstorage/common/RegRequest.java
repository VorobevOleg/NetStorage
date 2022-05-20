package ru.gb.java.netstorage.common;

import lombok.Getter;

@Getter
public class RegRequest implements BasicRequest{
    private String login;
    private String password;

    public RegRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getType() {
        return "regRequest";
    }


}
