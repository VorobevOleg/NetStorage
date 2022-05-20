module ru.gb.java.netstorage.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.transport;
    requires io.netty.codec;
    requires io.netty.buffer;
    requires ru.gb.java.netstorage.common;
    requires static lombok;

    opens ru.gb.java.netstorage.client to javafx.fxml;
    exports ru.gb.java.netstorage.client;


}
