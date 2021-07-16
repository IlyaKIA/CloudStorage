import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class Controller implements Initializable {

    public ListView<String> localFileList;
    public Label output;
    public ListView<String>  serverFileList;
    public Button deleteFile;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private String fileName;

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        try {
            fileName = localFileList.getSelectionModel().getSelectedItem();
            if(serverFileList.getItems().stream().noneMatch(p -> p.equals(fileName))) {
                os.writeObject(new FileMessage(Paths.get("dir", fileName))); //TODO: catalog address
                os.flush();
            } else {
                Platform.runLater(() -> output.setText("Status: File already exists"));
            }
        } catch (IOException e) {
            Platform.runLater(() -> output.setText("Status: Network error"));
        }
    }


    public void sendFromServer(ActionEvent actionEvent) {
        try {
            fileName = serverFileList.getSelectionModel().getSelectedItem();
            if(localFileList.getItems().stream().noneMatch(p -> p.equals(fileName))) {
                os.writeObject(new FileRequest(Paths.get("server_dir", fileName))); //TODO: set user catalog
                os.flush();
            } else {
                Platform.runLater(() -> output.setText("Status: File already exists"));
            }
        } catch (IOException e) {
            Platform.runLater(() -> output.setText("Status: Network error"));
        }
    }
    public void deleteFile(ActionEvent actionEvent) {
        //TODO: Checking  active list to delete item
        fileName = serverFileList.getSelectionModel().getSelectedItem();
        Paths.get("server_dir", fileName).toFile().delete();
        serverFileList.getItems().remove(fileName);
        Platform.runLater(() -> output.setText("Status: " + "File successfully deleted"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            File dir = new File("dir");
            localFileList.getItems().addAll(dir.list());
            os.writeObject(new ListMessage());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        AbstractCommand command = (AbstractCommand) is.readObject();
                        switch (command.getType()) {
                            case LIST_REQUEST:
                                ListRequest listRequest = (ListRequest) command;
                                Platform.runLater(() -> serverFileList.getItems().addAll(listRequest.getFileNameList()));
                                break;
                            case SIMPLE_MESSAGE:
                                Message message = (Message) command;
                                Platform.runLater(() -> output.setText("Status: " + message.toString()));
                                if (message.toString().equals("File sending successful")) {
                                    Platform.runLater(() -> serverFileList.getItems().add(fileName));
                                }
                                break;
                            case FILE_MESSAGE:
                                FileMessage inputFile = (FileMessage) command;
                                try (FileOutputStream fos = new FileOutputStream(Paths.get("dir", inputFile.getName()).toString())) {
                                    fos.write(inputFile.getData());
                                    Platform.runLater(() -> {
                                        localFileList.getItems().add(inputFile.getName());
                                        //TODO: Sort file list
                                        output.setText("Status: " + "File successful downloaded");
                                    });

                                } catch (Exception e) {
                                    Platform.runLater(() -> output.setText("Status:" + "File download error"));
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
            Platform.runLater(() -> output.setText("Status: Connected"));
        } catch (Exception e) {
            Platform.runLater(() -> output.setText("Status: Connection failed"));
        }
    }

    public void exitApp(MouseEvent mouseEvent) {
    }


}
