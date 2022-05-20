package ru.gb.java.netstorage.client;

import java.nio.file.Path;

public class ClientInfo {
    private static final ClientInfo INSTANCE = new ClientInfo();
    private static String login;
    private static String password;
    private static Path currentPath;
    private static Path rootClientPath;
    private static int maxFolderDepth;

    public static ClientInfo getInstance() {
        return INSTANCE;
    }

    public static void setCurrentPath(Path currentPath) {
        ClientInfo.currentPath = currentPath;
    }

    public static Path getCurrentPath() {
        return currentPath;
    }

    public static String getLogin() {
        return login;
    }

    public static String getPassword() {
        return password;
    }

    public static void setLogin(String login) {
        ClientInfo.login = login;
    }

    public static void setPassword(String password) {
        ClientInfo.password = password;
    }

    public static void setMaxFolderDepth(int maxFolderDepth) {
        ClientInfo.maxFolderDepth = maxFolderDepth;
    }

    public static int getMaxFolderDepth() {
        return maxFolderDepth;
    }

    public static Path getRootClientPath() {
        return rootClientPath;
    }

    public static void setRootClientPath(Path rootClientPath) {
        ClientInfo.rootClientPath = rootClientPath;
    }
}
