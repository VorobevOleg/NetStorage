package ru.gb.java.netstorage.client;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import ru.gb.java.netstorage.common.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PanelRightController implements Initializable {
    @FXML
    TableView<FileInfo> filesTable;
    @FXML
    TextField pathField;
    @FXML
    Button btnUp;
    private Controller mainPC;
    private final ClientNettyConnection clientNettyConnection = ClientNettyConnection.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);

        Image dirImage = new Image("/folder-icon.png");
        Image fileImage = new Image("/file-icon.png");

        TableColumn<FileInfo, ImageView> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getType() == FileInfo.FileType.DIRECTORY ? new ImageView(dirImage) : new ImageView(fileImage)));
        fileTypeColumn.setPrefWidth(32);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long aLong, boolean b) {
                    super.updateItem(aLong, b);
                    if (aLong == null || b) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", aLong);
                        if (aLong == -1) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);

        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);
        filesTable.getSortOrder().add(fileSizeColumn);

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText() + filesTable.getSelectionModel().getSelectedItem().getFileName());
                    clientNettyConnection.sendMessage(
                            new OpenDirRequest(
                            new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                            path));
                }
            }
        });
        clientNettyConnection.sendMessage(
                new GetFirstFileListRequest(
                new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword())));
    }

    public void updateList(GetFileListResponse fileListResponse) {
        try {
            Initializable mainController = ControllerRegistry.getControllerObject(Controller.class);
            mainPC = (Controller) mainController;
            String currentStrPath = fileListResponse.getPathFileListResponse().substring(14) + "\\";
            pathField.setText(currentStrPath);
            ClientInfo.setCurrentPath(Paths.get(currentStrPath));
            btnUp.setDisable(currentStrPath.equals(ClientInfo.getLogin() + "\\"));
            mainPC.createDirBtn.setDisable(!checkMaxFolderDepth()); // если вложенность папки больше 10
            filesTable.getItems().clear();
            filesTable.getItems().addAll(fileListResponse.getFileListResponse());
            filesTable.sort();
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            clientNettyConnection.sendMessage(new GetFileListRequest(
                    new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                    upperPath));
        }
    }

    public String getSelectedFilename() {
        if (!filesTable.isFocused()) {
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }

    public void btnRefreshAction(ActionEvent actionEvent) {
        clientNettyConnection.sendMessage(new GetFileListRequest(
                new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                Paths.get(pathField.getText())));
    }

    public boolean checkMaxFolderDepth () {
        int depth = 0;
        String rootDir = ClientInfo.getLogin();
        Path parentPatn = ClientInfo.getCurrentPath();
        while (!parentPatn.toString().equals(rootDir)) {
            depth++;
            parentPatn = parentPatn.getParent();
        }
        return depth < ClientInfo.getMaxFolderDepth();
    }
}
