package ru.gb.java.netstorage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import  ru.gb.java.netstorage.common.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

public class BasicHandler extends ChannelInboundHandlerAdapter {
    private final DbService dbService = DbService.getInstance();
    private static final String ROOT_DIR = "ServerStorage/";
    private static final int MAX_FOLDER_DEPTH = 10;
    private static long maxUserStorageSize;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент подключился: " + ctx.channel().remoteAddress());
        System.out.println("Клиент подключился на: " + ctx.channel().localAddress());
        System.out.println("Канал открыт (полная информация): " + ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {

        //Обработка запросов получения списка файлов при авторизации / регистрации
        if (request instanceof GetFirstFileListRequest) {
            AuthRequest authRequest = ((GetFirstFileListRequest) request).getAuthRequest();
            String userStrRootDir = ROOT_DIR + authRequest.getLogin();
            Path userPathRootDir = Paths.get(".", userStrRootDir);
            if(Files.exists(userPathRootDir)) {
                GetFileListResponse getFileListResponse = new GetFileListResponse(userPathRootDir);
                ctx.writeAndFlush(getFileListResponse);
                return;
            } else {
                try {
                    Files.createDirectory(userPathRootDir);
                    GetFileListResponse getFileListResponse = new GetFileListResponse(userPathRootDir);
                    ctx.writeAndFlush(getFileListResponse);
                    return;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        //Обработка запросов получения списка файлов
        if (request instanceof GetFileListRequest) {
            GetFileListRequest getFileListRequest = (GetFileListRequest)request;
            AuthRequest authRequest = getFileListRequest.getAuthRequest();
            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), getFileListRequest.getPathStr())) {
                String responsePathStr = ROOT_DIR + getFileListRequest.getPathStr();
                Path responsePath = Paths.get(".", responsePathStr);
                GetFileListResponse getFileListResponse = new GetFileListResponse(responsePath);
                ctx.writeAndFlush(getFileListResponse);
            }
            return;
        }

        //Обработка запросов получения списка файлов при открытии папки
        if (request instanceof OpenDirRequest) {
            AuthRequest authRequest = ((OpenDirRequest) request).getAuthRequest();
            String requestPathStr = ((OpenDirRequest) request).getPathStr();
            String responsePathStr = ROOT_DIR + requestPathStr;
            Path responsePath = Paths.get(".", responsePathStr);
            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), requestPathStr) && Files.isDirectory(responsePath)) {
                GetFileListResponse getFileListResponse = new GetFileListResponse(responsePath);
                ctx.writeAndFlush(getFileListResponse);
            }
            return;
        }

        //Обработка запросов скачивания файлов
        if (request instanceof DownloadFileRequest) {
            AuthRequest authRequest = ((DownloadFileRequest) request).getAuthRequest();
            String requestPathToFileStr = ((DownloadFileRequest) request).getPathToFileStr();
            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), requestPathToFileStr)) {
                String responsePathToFileStr = ROOT_DIR + requestPathToFileStr;
                Path responsePath = Paths.get(".", responsePathToFileStr);
                File file = new File(String.valueOf(responsePath));
                FileSplit fileSplit = new FileSplit();
                fileSplit.split(responsePath, (bytes, lenBytes) -> ctx.writeAndFlush(new FilePartResponse(file.getName(),file.length(),bytes, lenBytes)));
            }
            return;
        }

        //Обработка запросов загрузки файлов
        if (request instanceof UploadFileRequest) {
            AuthRequest authRequest = ((UploadFileRequest) request).getAuthRequest();
            UploadFileRequest uploadFileRequest = (UploadFileRequest)request;
            String requestPathToFileStr = ((UploadFileRequest) request).getPathToUploadFileStr();
            String requestPathToLocalFileStr = ((UploadFileRequest) request).getPathToLocalFileStr();
            long fileLength = uploadFileRequest.getFileLength();
            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), requestPathToFileStr)) {
                String responsePathToFileStr = ROOT_DIR + requestPathToFileStr;
                Path responsePath = Paths.get(".", responsePathToFileStr);
                Path rootUserDir = Paths.get(ROOT_DIR + requestPathToFileStr.substring(0,authRequest.getLogin().length()));
                if (!Files.exists(responsePath) && (fileLength <= (maxUserStorageSize - getSizeOfAllUserFiles(rootUserDir)))) {
                   ctx.writeAndFlush(new UploadFilePesponse(requestPathToLocalFileStr, requestPathToFileStr, false, false));
                   return;
                }
                if (Files.exists(responsePath)) {
                    ctx.writeAndFlush(new UploadFilePesponse(requestPathToLocalFileStr, requestPathToFileStr, true, false));
                    return;
                }
                if (!(fileLength <= (maxUserStorageSize - getSizeOfAllUserFiles(rootUserDir)))) {
                    ctx.writeAndFlush(new UploadFilePesponse(requestPathToLocalFileStr, requestPathToFileStr, false,true));
                    return;
                }
            }
            return;
        }

        //Обработка загрузки части файлов
        if (request instanceof FilePartResponse) {
            FilePartResponse filePartResponse = (FilePartResponse)request;
            String fileName = filePartResponse.getFileName();
            String pathToUploadFileStr = ROOT_DIR + filePartResponse.getPathToStr();
            long fileLengh = filePartResponse.getFileLength();
            byte[] partBytes = filePartResponse.getPartBytes();
            int partBytesLen = filePartResponse.getPartBytesLen();
            Path pathToUploadFile = Paths.get(".", pathToUploadFileStr);
            File file = new File(String.valueOf(pathToUploadFile));
            try (FileOutputStream outputStream = new FileOutputStream(file, true)) {
                outputStream.write(partBytes, 0, partBytesLen);
                if (file.length() >= fileLengh) {
                    ctx.writeAndFlush(new GetFileListResponse(
                            Paths.get(".",pathToUploadFileStr.substring(0,pathToUploadFileStr.length()-fileName.length()))));
                }
            }
            return;
        }

        //Обработка запросов на создание новой папки
        if (request instanceof CreateDirRequest) {
            AuthRequest authRequest = ((CreateDirRequest) request).getAuthRequest();
            String requestPathStr = ((CreateDirRequest) request).getPathStr();
            String responsePathStr = ROOT_DIR + requestPathStr;
            Path responsePath = Paths.get(".", responsePathStr);
            File newDir = new File(String.valueOf(responsePath));
            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), requestPathStr) && !newDir.exists()) {
                if (newDir.mkdir()) {
                    ctx.writeAndFlush(
                            new CreateDirResponse(
                                    new GetFileListResponse(responsePath.getParent()),
                                    true));
                    return;
                }
            }
            ctx.writeAndFlush(
                    new CreateDirResponse(
                            new GetFileListResponse(responsePath.getParent()),
                            false));
            return;
        }

        //Обработка запросов на удаление файла или папки
        if (request instanceof DeleteFileRequest) {
            AuthRequest authRequest = ((DeleteFileRequest) request).getAuthRequest();
            String requestPathStr = ((DeleteFileRequest) request).getPathStr();
            String responsePathStr = ROOT_DIR + requestPathStr;
            Path responsePath = Paths.get(".", responsePathStr);
            Path parentResponsePath = responsePath.getParent();
            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), requestPathStr)) {
                if (!Files.isDirectory(responsePath)) {
                    try {
                        Files.delete(responsePath);
                        ctx.writeAndFlush(
                                new DeleteFileResponse(
                                        new GetFileListResponse(parentResponsePath),
                                        true));
                        return;
                    } catch (IOException e) {
                        ctx.writeAndFlush(
                                new DeleteFileResponse(
                                        new GetFileListResponse(parentResponsePath),
                                        false));
                        return;
                    }
                } else {
                    try {
                    FileUtils.deleteDirectory(new File(String.valueOf(responsePath)));
                        ctx.writeAndFlush(
                                new DeleteFileResponse(
                                        new GetFileListResponse(parentResponsePath),
                                        true));
                        return;
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        ctx.writeAndFlush(
                                new DeleteFileResponse(
                                        new GetFileListResponse(parentResponsePath),
                                        false));
                    }
                }
            }
            return;
        }

        //Обработка запросов на удаление файла при замене
        if (request instanceof DeleteFileFromUploadRequest) {
            AuthRequest authRequest = ((DeleteFileFromUploadRequest) request).getAuthRequest();
            String requestPathStr = ((DeleteFileFromUploadRequest) request).getPathStr();
            String pathToLocalFileStr = ((DeleteFileFromUploadRequest) request).getPathToLocalFileStr();
            String responsePathStr = ROOT_DIR + requestPathStr;
            Path responsePath = Paths.get(".", responsePathStr);
            Path parentResponsePath = responsePath.getParent();
            if (checkAuth(authRequest) && checkPathRights(authRequest.getLogin(), requestPathStr)) {
                if (!Files.isDirectory(responsePath)) {
                    try {
                        Files.delete(responsePath);
                        ctx.writeAndFlush(
                                new DeleteFileFromUploadResponse(
                                        new GetFileListResponse(parentResponsePath),
                                        true, pathToLocalFileStr, requestPathStr));
                        return;
                    } catch (IOException e) {
                        ctx.writeAndFlush(
                                new DeleteFileFromUploadResponse(
                                        new GetFileListResponse(parentResponsePath),
                                        false, pathToLocalFileStr, requestPathStr));
                        return;
                    }
                }
            }
            return;
        }

        //Обработка запросов авторизации
        if (request instanceof AuthRequest) {
            if (checkAuth((AuthRequest)request)) {
                AuthResponse authResponse = new AuthResponse(true);
                authResponse.setMaxFolderDepth(MAX_FOLDER_DEPTH);
                maxUserStorageSize = dbService.getMaxStorageSizeByLogin(((AuthRequest) request).getLogin());
                ctx.writeAndFlush(authResponse);
                System.out.println("Клиент " + ((AuthRequest) request).getLogin() + " залогинился");
                return;
            } else {
                ctx.writeAndFlush(new AuthResponse(false));
                System.out.println("Клиент " + ((AuthRequest) request).getLogin() + " пытался залогиниться");
                return;
            }
        }

        //Обработка запросов регистрации нового пользователя
        if (request instanceof RegRequest) {
            if (!dbService.isInDb(((RegRequest) request).getLogin())) {
                if (dbService.registration(((RegRequest) request).getLogin(), ((RegRequest) request).getPassword())) {
                    RegResponse regResponse = new RegResponse(true);
                    maxUserStorageSize = dbService.getMaxStorageSizeByLogin(((RegRequest) request).getLogin());
                    regResponse.setMaxFolderDepth(MAX_FOLDER_DEPTH);
                    ctx.writeAndFlush(regResponse);
                } else {
                    ctx.writeAndFlush(new RegResponse(false));
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Клиент "+ ctx.channel().remoteAddress() + " отвалился -_-");
        ctx.close();
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        System.out.println("Канал клиента "  + ctx.channel().remoteAddress() + " закрыт");
    }

    public boolean checkAuth (AuthRequest authRequest) {
        String login = authRequest.getLogin();
        String password = authRequest.getPassword();
        return login.equals(dbService.getLoginByPass(login, password.hashCode()));
    }

    public boolean checkPathRights (String login, String pathStr) {
        return  (login.equals(pathStr.substring(0,login.length())));
    }

    public long getSizeOfAllUserFiles (Path path) throws IOException {
         try (Stream<Path> walk = Files.walk(path)) {
             return walk.map(Path::toFile)
                        .filter(File::isFile)
                        .mapToLong(File::length)
                        .sum();
        }

    }
}
