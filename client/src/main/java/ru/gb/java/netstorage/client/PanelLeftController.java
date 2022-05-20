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
import ru.gb.java.netstorage.common.FileInfo;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PanelLeftController implements Initializable {
    @FXML
    TableView<FileInfo> filesTable;
    @FXML
    ComboBox<String> disksBox;
    @FXML
    TextField pathField;

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

        disksBox.getItems().clear();
        for (Path p: FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });

        Path rootClientPath = Paths.get(".","ClientStorage");

        if(Files.exists(rootClientPath)) {
            ClientInfo.setRootClientPath(rootClientPath);
            updateList(rootClientPath);
        } else {
            try {
                Files.createDirectory(rootClientPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateList(Path path) {
        try (Stream<Path> list = Files.list(path)) {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(list.map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>)actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
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
        updateList(Paths.get(pathField.getText()));
    }

}
