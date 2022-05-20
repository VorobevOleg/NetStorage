package ru.gb.java.netstorage.client;

import javafx.fxml.Initializable;
import java.util.HashMap;
import java.util.Map;

public class ControllerRegistry {
    private static final Map<Class<Initializable>, Initializable> CONTROLLER_REGISTRY = new HashMap<>();

    public static void register(Initializable controller) {
        CONTROLLER_REGISTRY.put((Class<Initializable>) controller.getClass(), controller);
    }

    public static Initializable getControllerObject(Class<? extends Initializable> controller) {
        return CONTROLLER_REGISTRY.get(controller);
    }
}
