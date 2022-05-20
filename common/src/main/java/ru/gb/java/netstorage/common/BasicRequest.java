package ru.gb.java.netstorage.common;

import java.io.Serializable;

public interface BasicRequest extends Serializable {
    String getType();
}
