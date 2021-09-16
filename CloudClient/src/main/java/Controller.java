import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class Controller implements Initializable {

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    public ListView<String> localFileList;
    public ListView<String>  serverFileList;
    public Label output;
    public TextField serverPath;
    public TextField  clientPath;
    private String fileName;
    private Path currentDir;

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        try {
            fileName = localFileList.getSelectionModel().getSelectedItem();
            if(serverFileList.getItems().stream().noneMatch(p -> p.equals(fileName))) {
                os.writeObject(new FileMessage(Paths.get(currentDir.toString(), fileName))); //TODO: catalog address
                os.flush();
            } else {
                statusUpdater("File already exists");
            }
        } catch (IOException e) {
            statusUpdater("Network error");
        }
    }


    public void downloadFromServer(ActionEvent actionEvent) {
        try {
            fileName = serverFileList.getSelectionModel().getSelectedItem();
            if(localFileList.getItems().stream().noneMatch(p -> p.equals(fileName))) {
                os.writeObject(new FileRequest(fileName)); //TODO: set user catalog
                os.flush();
            } else {
                statusUpdater("File already exists");
            }
        } catch (IOException e) {
            statusUpdater("Network error");
        }
    }
    public void deleteServerFile(ActionEvent actionEvent) {
        fileName = serverFileList.getSelectionModel().getSelectedItem();
        try {
            os.writeObject(new DeleteRequest(fileName));
            os.flush();

        } catch (IOException e) {
            statusUpdater("Error deleting file from server");
        }
    }
    public void deleteLocalFile(ActionEvent actionEvent) {
        fileName = localFileList.getSelectionModel().getSelectedItem();
        boolean localFileDeletingStatus;
        localFileDeletingStatus = currentDir.resolve(fileName).toFile().delete();
        if(localFileDeletingStatus) statusUpdater("Local file " + fileName + " successfully deleted");
        else statusUpdater("Local file " + fileName + " deleting error");
        try {
            refreshClientView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            currentDir = Paths.get("dir").toAbsolutePath();
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            refreshClientView();
            addNavigationListeners();
            os.writeObject(new ListMessage());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        AbstractCommand command = (AbstractCommand) is.readObject();
                        switch (command.getType()) {
                            case LIST_RESPONSE:
                                ListResponse listResponse = (ListResponse) command;
                                List<String> serverFilesNames = new ArrayList<>();
                                if(!listResponse.isRoot()){
                                    serverFilesNames.add("..");
                                }
                                serverFilesNames.addAll(listResponse.getFileNameList());
                                refreshServerView(serverFilesNames);
                                break;
                            case PATH_RESPONSE:
                                PathUpResponse pathResponse = (PathUpResponse) command;
                                String path = pathResponse.getPath();
                                Platform.runLater(() -> serverPath.setText(path));
                                break;
                            case SIMPLE_MESSAGE:
                                Message message = (Message) command;
                                statusUpdater(message.toString());
                                break;
                            case FILE_MESSAGE:
                                FileMessage inputFile = (FileMessage) command;
                                try (FileOutputStream fos = new FileOutputStream(currentDir.resolve(inputFile.getName()).toString())) {
                                    fos.write(inputFile.getData());
                                    refreshClientView();
                                    Platform.runLater(() -> {
                                        //TODO: Sort file list
                                        statusUpdater("File successful downloaded");
                                    });

                                } catch (Exception e) {
                                    statusUpdater("File download error");
                                }
                                break;
                         }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readThread.setDaemon(true);
            readThread.start();
            statusUpdater("Connected");
        } catch (Exception e) {
            statusUpdater("Connection failed");
        }
    }

    public void exitApp(MouseEvent mouseEvent) {
    }

    private void refreshClientView() throws IOException {
        clientPath.setText(currentDir.toString());

        List<String> names = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            localFileList.getItems().clear();
            localFileList.getItems().add("..");
            localFileList.getItems().addAll(names);
        });
    }
    private void refreshServerView(List<String> names) {
        Platform.runLater(() -> {
            serverFileList.getItems().clear();
            serverFileList.getItems().addAll(names);
        });
    }

    private void addNavigationListeners() {
        localFileList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = localFileList.getSelectionModel().getSelectedItem();
                Path newPath;
                if (item.equals("..")) {
                    newPath= currentDir.getParent();
                } else {
                    newPath = currentDir.resolve(item);
                }
                if (Files.isDirectory(newPath)) {
                    currentDir = newPath;
                    try {
                        refreshClientView();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        serverFileList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = serverFileList.getSelectionModel().getSelectedItem();
                try {
                    if (item.equals("..")){
                        os.writeObject(new PathUpRequest());
                        os.flush();
                    } else {
                        os.writeObject(new PathInRequest(item));
                        os.flush();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }
    private void statusUpdater (String msg){
        Platform.runLater(() -> output.setText("Status: " + msg));
    }
}
