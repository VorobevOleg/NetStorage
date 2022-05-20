package ru.gb.java.netstorage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ru.gb.java.netstorage.common.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BasicResponse response = (BasicResponse) msg;
        System.out.println("Получен ответ от сервера типа: " + response.getType());

        //Обработка ответов получения списка файлов
        if (response instanceof GetFileListResponse) {
            Initializable rightPanel = ControllerRegistry.getControllerObject(PanelRightController.class);
            PanelRightController rightPC = (PanelRightController) rightPanel;
            Platform.runLater(() -> {
                rightPC.updateList((GetFileListResponse)response);
            });
            return;
        }

        //Обработка ответов скачивания файла
        if (response instanceof FilePartResponse) {
            FilePartResponse filePartResponse = (FilePartResponse)response;
            String fileName = filePartResponse.getFileName();
            long fileLengh = filePartResponse.getFileLength();
            byte[] partBytes = filePartResponse.getPartBytes();
            int partBytesLen = filePartResponse.getPartBytesLen();
            Path pathToFile = ClientInfo.getRootClientPath().resolve(fileName);
            File file = new File(String.valueOf(pathToFile));
            try (FileOutputStream outputStream = new FileOutputStream(file, true)) {
                outputStream.write(partBytes, 0, partBytesLen);
                if (file.length() >= fileLengh) {
                    Initializable leftPanel = ControllerRegistry.getControllerObject(PanelLeftController.class);
                    PanelLeftController leftPC = (PanelLeftController) leftPanel;
                    Platform.runLater(() -> {
                        leftPC.updateList(Paths.get(leftPC.getCurrentPath()));
                    });
                }
            }
            return;
        }

        //Обработка ответов загрузки файла
        if (response instanceof UploadFilePesponse) {
            UploadFilePesponse uploadFilePesponse = (UploadFilePesponse)response;
            String pathToLocalFileStr = uploadFilePesponse.getPathToLocalFileStr();
            String pathToUploadFileStr = uploadFilePesponse.getPathToUploadFileStr();
            boolean needDeleteFile = uploadFilePesponse.isNeedDeleteFile();
            boolean noFreeStorage = uploadFilePesponse.isNoFreeStorage();
            File file = new File(pathToLocalFileStr);

            if (!needDeleteFile && !noFreeStorage) {
                FileSplit fileSplit = new FileSplit();
                fileSplit.split(Paths.get(pathToLocalFileStr), (bytes, lenBytes) -> {
                    FilePartResponse filePartResponse = new FilePartResponse(file.getName(), file.length(), bytes, lenBytes);
                    filePartResponse.setPathToStr(pathToUploadFileStr);
                    ctx.writeAndFlush(filePartResponse);
                    });
                return;
            }

            if (noFreeStorage) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Недостаточно свободного места", ButtonType.OK);
                    alert.showAndWait();
                });
                return;
            }

            if (needDeleteFile) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Заменить файл?");
                    alert.setHeaderText("Файл '" + file.getName() + "' существует. Заменить файл?");
                    alert.setContentText(pathToUploadFileStr);
                    Optional<ButtonType> option = alert.showAndWait();
                    if (option.get() == ButtonType.OK) {
                        ctx.writeAndFlush(
                                new DeleteFileFromUploadRequest(
                                        new AuthRequest(ClientInfo.getLogin(), ClientInfo.getPassword()),
                                        pathToUploadFileStr, pathToLocalFileStr));
                    }
                });
            }
            return;
        }

        //Обработка ответов загрузки файла при замене
        if (response instanceof DeleteFileFromUploadResponse) {
            DeleteFileFromUploadResponse deleteFileFromUploadResponse = (DeleteFileFromUploadResponse)response;
            String pathToLocalFileStr = deleteFileFromUploadResponse.getPathToLocalFileStr();
            String pathToUploadFileStr = deleteFileFromUploadResponse.getPathToUploadFileStr();
            File file = new File(pathToLocalFileStr);
            Platform.runLater(() -> {
                if (deleteFileFromUploadResponse.isDeleteFileOk()) {
                    Initializable rightPanel = ControllerRegistry.getControllerObject(PanelRightController.class);
                    PanelRightController rightPC = (PanelRightController) rightPanel;
                    rightPC.updateList(deleteFileFromUploadResponse.getGetFileListResponse());
                    FileSplit fileSplit = new FileSplit();
                    fileSplit.split(Paths.get(pathToLocalFileStr), (bytes, lenBytes) -> {
                        FilePartResponse filePartResponse = new FilePartResponse(file.getName(), file.length(), bytes, lenBytes);
                        filePartResponse.setPathToStr(pathToUploadFileStr);
                        ctx.writeAndFlush(filePartResponse);
                    });
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно удалить выбранный файл", ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return;
        }

        //Обработка ответов на создание новой папки
        if (response instanceof CreateDirResponse) {
            CreateDirResponse createDirResponse = (CreateDirResponse)response;
            Platform.runLater(() -> {
                if (createDirResponse.isCreateDirOk()) {
                    Initializable rightPanel = ControllerRegistry.getControllerObject(PanelRightController.class);
                    PanelRightController rightPC = (PanelRightController) rightPanel;
                    rightPC.updateList(createDirResponse.getGetFileListResponse());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось создать папку", ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return;
        }

        //Обработка ответов на удаление файла
        if (response instanceof DeleteFileResponse) {
            DeleteFileResponse deleteFileResponse = (DeleteFileResponse)response;
            Platform.runLater(() -> {
                if (deleteFileResponse.isDeleteFileOk()) {
                    Initializable rightPanel = ControllerRegistry.getControllerObject(PanelRightController.class);
                    PanelRightController rightPC = (PanelRightController) rightPanel;
                    rightPC.updateList(deleteFileResponse.getGetFileListResponse());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно удалить выбранный файл", ButtonType.OK);
                    alert.showAndWait();
                }
            });
            return;
        }

        //Обработка ответов авторизации
        if (response instanceof AuthResponse && (((AuthResponse) response).isAuthOk())) {
            ClientInfo.setMaxFolderDepth(((AuthResponse) response).getMaxFolderDepth());
            ClientApp.setRoot("main");
            return;
        }
        if (response instanceof AuthResponse && !(((AuthResponse) response).isAuthOk())) {
            AuthController authController = (AuthController)ControllerRegistry.getControllerObject(AuthController.class);
            Platform.runLater(() -> {
                authController.setAuthErrorLabel("Неверный логин / пароль");
            });
            return;
        }

        //Обработка ответов регистрации
        if (response instanceof RegResponse && ((RegResponse) response).isRegOk()) {
            ClientInfo.setMaxFolderDepth(((RegResponse) response).getMaxFolderDepth());
            ClientApp.setRoot("main");
            return;
        }
        if (response instanceof RegResponse && !((RegResponse) response).isRegOk()) {
            AuthController authController = (AuthController)ControllerRegistry.getControllerObject(AuthController.class);
            Platform.runLater(() -> {
                authController.setAuthErrorLabel("Регистрация не удалась");
            });
        }
    }
}
