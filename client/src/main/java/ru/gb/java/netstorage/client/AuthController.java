package ru.gb.java.netstorage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.gb.java.netstorage.common.AuthRequest;
import ru.gb.java.netstorage.common.RegRequest;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    private final ClientNettyConnection clientNettyConnection = ClientNettyConnection.getInstance();
    @FXML
    TextField textFieldLogin;
    @FXML
    PasswordField passwordField;
    @FXML
    Label authErrorLabel;
    public void setAuthErrorLabel(String authErrorLabel) {
        this.authErrorLabel.setText(authErrorLabel);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);
        textFieldLogin.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    authBtnAction(new ActionEvent());
                    event.consume();
                }
            }
        });
        passwordField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    authBtnAction(new ActionEvent());
                    event.consume();
                }
            }
        });
    }

    public void authBtnAction(ActionEvent actionEvent) {
        String login = textFieldLogin.getText().trim();
        String password = passwordField.getText().trim();
        if (login.isEmpty() || login.isBlank()) {
            this.setAuthErrorLabel("Введите логин");
            return;
        }
        if (password.isEmpty() || password.isBlank()) {
            this.setAuthErrorLabel("Введите пароль");
            return;
        }
        ClientInfo.setLogin(login);
        ClientInfo.setPassword(password);
        clientNettyConnection.sendMessage(new AuthRequest(login,password));
    }

    public void regBtnAction(ActionEvent actionEvent) {
        String login = textFieldLogin.getText().trim();
        String password = passwordField.getText().trim();
        if (login.isEmpty() || login.isBlank()) {
            this.setAuthErrorLabel("Введите логин");
            return;
        }
        if (password.isEmpty() || password.isBlank()) {
            this.setAuthErrorLabel("Введите пароль");
            return;
        }
        clientNettyConnection.sendMessage(new RegRequest(login,password));
    }

    public void exitBtnAction() {
        clientNettyConnection.close();
        Platform.exit();
    }
}
