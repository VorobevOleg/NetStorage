package ru.gb.java.netstorage.server;

public class StartNettyServer {
    public static void main(String[] args) throws InterruptedException {
        new NettyServer().run();
    }
}
