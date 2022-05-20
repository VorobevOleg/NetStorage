module ru.gb.java.net.storage {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.transport;
    requires io.netty.codec;
    requires io.netty.buffer;

    opens ru.gb.java.netstorage.client to javafx.fxml;
}
