package ru.gb.java.netstorage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import ru.gb.java.netstorage.common.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private PanelLeftController leftPC;
    private PanelRightController rightPC;
    @FXML
    Button createDirBtn;
    private final ClientNettyConnection clientNettyConnection = ClientNettyConnection.getInstance();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);
    }

    public void downloadUploadBtnAction(ActionEvent actionEvent) {
        Initializable leftPanel = ControllerRegistry.getControllerObject(PanelLeftController.class);
        Initializable rightPanel = ControllerRegistry.getControllerObject(PanelRightController.class);
        leftPC = (PanelLeftController) leftPanel;
        rightPC = (PanelRightController) rightPanel;
        if (leftPC.getSelectedFilename() == null && rightPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл для загрузки / скачивания не выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        //Загрузка файла на сервер
        if (leftPC.getSelectedFilename() != null) {
            String fileName = leftPC.getSelectedFilename();
            Path pathToLocalFile = Paths.get(leftPC.getCurrentPath()).resolve(fileName);
            Path pathToUploadFile = Paths.get(rightPC.getCurrentPath()).resolve(fileName);
            if (!Files.isDirectory(pathToLocalFile) ) {
                File file = new File(String.valueOf(pathToLocalFile));
                clientNettyConnection.sendMessage(new UploadFileRequest(
                        new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                        pathToUploadFile, pathToLocalFile, file.length()));
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Выберите файл, а не папку " +
                        "- у приложения пока лапки...", ButtonType.OK);
                alert.showAndWait();
            }
        }

        //Скачивание файла с сервера
        if (rightPC.getSelectedFilename() != null) {
            String fileName = rightPC.getSelectedFilename();
            Path pathToDownloadFile = Paths.get(rightPC.getCurrentPath()).resolve(fileName);
            Path pathToLocalFile = ClientInfo.getRootClientPath().resolve(fileName);
            if (Files.exists(pathToLocalFile)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Заменить файл?");
                alert.setHeaderText("Файл '" + fileName + "' существует. Заменить файл?");
                alert.setContentText(pathToLocalFile.toAbsolutePath().normalize().toString());
                Optional<ButtonType> option = alert.showAndWait();
                if (option.isPresent()) {
                    if (option.get() == ButtonType.OK) {
                        try {
                            Files.delete(pathToLocalFile);
                            leftPC.updateList(ClientInfo.getCurrentPath());
                            clientNettyConnection.sendMessage(
                                    new DownloadFileRequest(
                                            new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                            pathToDownloadFile));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
            clientNettyConnection.sendMessage(
                    new DownloadFileRequest(
                            new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                            pathToDownloadFile));
        }
    }

    public void createDirBtnAction(ActionEvent actionEvent) throws InterruptedException {
        Initializable rightPanel = ControllerRegistry.getControllerObject(PanelRightController.class);
        rightPC = (PanelRightController) rightPanel;
        TextInputDialog textInputDialog = new TextInputDialog("Новая папка");
        textInputDialog.setTitle("Создание новой папки");
        textInputDialog.setHeaderText(null);
        textInputDialog.setContentText("Введите имя:");
        Optional<String> resultDialog = textInputDialog.showAndWait();
        if (resultDialog.isPresent()) {
            String nameNewDir = resultDialog.get().replaceAll("[^A-Za-zА-Яа-я0-9 ]", "");
                Path pathToNewDir = Paths.get(rightPC.getCurrentPath()).resolve(nameNewDir);
                clientNettyConnection.sendMessage(
                        new CreateDirRequest(
                                new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                pathToNewDir));
        }
    }

    public void deleteBtnAction(ActionEvent actionEvent) {
        Initializable leftPanel = ControllerRegistry.getControllerObject(PanelLeftController.class);
        Initializable rightPanel = ControllerRegistry.getControllerObject(PanelRightController.class);
        leftPC = (PanelLeftController) leftPanel;
        rightPC = (PanelRightController) rightPanel;
        if (leftPC.getSelectedFilename() == null && rightPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл для удаления не выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (leftPC.getSelectedFilename() != null) {
            Path pathToFile = Paths.get(leftPC.getCurrentPath()).resolve(leftPC.getSelectedFilename());
            if(!Files.isDirectory(pathToFile)) {
                try {
                    Files.delete(pathToFile);
                    leftPC.updateList(Paths.get(leftPC.getCurrentPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно удалить выбранный файл", ButtonType.OK);
                    alert.showAndWait();
                }
            }
        }
        if (rightPC.getSelectedFilename() != null) {
            Path pathToFile = Paths.get(rightPC.getCurrentPath()).resolve(rightPC.getSelectedFilename());
            clientNettyConnection.sendMessage(
                    new DeleteFileRequest(
                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                    pathToFile));
        }
    }

    public void menuItemExit(ActionEvent actionEvent) {
        clientNettyConnection.close();
        Platform.exit();
    }
}

