import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@Slf4j
public class ClientController implements Initializable {


    public ListView<String> localFileList;
    public ListView<String>  serverFileList;
    public Label output;
    public TextField serverPath;
    public TextField  clientPath;
    private String fileName;
    private Path currentDir;
    private final ClientMessageHandler messHandler;

    public ClientController() {
        messHandler = App.messHandler;
    }
    @FXML
    public void sendToServer() {
        try {
            fileName = localFileList.getSelectionModel().getSelectedItem();
            if(serverFileList.getItems().stream().noneMatch(p -> p.equals(fileName))) {
                messHandler.getOs().writeObject(new FileMessage(Paths.get(currentDir.toString(), fileName))); //TODO: catalog address
                messHandler.getOs().flush();
            } else {
                statusUpdater("File already exists");
            }
        } catch (IOException e) {
            statusUpdater("Network error");
        }
    }

    @FXML
    public void downloadFromServer() {
        try {
            fileName = serverFileList.getSelectionModel().getSelectedItem();
            if(localFileList.getItems().stream().noneMatch(p -> p.equals(fileName))) {
                messHandler.getOs().writeObject(new FileRequest(fileName)); //TODO: set user catalog
                messHandler.getOs().flush();
            } else {
                statusUpdater("File already exists");
            }
        } catch (IOException e) {
            statusUpdater("Network error");
        }
    }
    @FXML
    public void deleteServerFile() {
        fileName = serverFileList.getSelectionModel().getSelectedItem();
        try {
            messHandler.getOs().writeObject(new DeleteRequest(fileName));
            messHandler.getOs().flush();

        } catch (IOException e) {
            statusUpdater("Error deleting file from server");
        }
    }
    @FXML
    public void deleteLocalFile() {
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


    @FXML
    public void mkServerDir() throws IOException {
        newDialogWindow("Insert name of new directory", DialogWinCommand.CREATE_SERVER_DIR);
    }
    @FXML
    public void mkLocalDir() throws IOException {
        newDialogWindow("Insert name of new directory", DialogWinCommand.CREATE_LOCAL_DIR);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            String userDir = System.getProperty("user.name");
            currentDir = Paths.get("/Users", userDir).toAbsolutePath();
            refreshClientView();
            addNavigationListeners();
            messHandler.getOs().writeObject(new ListMessage());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        AbstractCommand command = (AbstractCommand) messHandler.getIs().readObject();
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
                        messHandler.getOs().writeObject(new PathUpRequest());
                    } else {
                        messHandler.getOs().writeObject(new PathInRequest(item));
                    }
                    messHandler.getOs().flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }
    private void statusUpdater (String msg){
        Platform.runLater(() -> output.setText("Status: " + msg));
    }

    public void newDialogWindow(String msg, DialogWinCommand dwc) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("InsertNameDialog.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene (new Scene(root));
        stage.getIcons().add(new Image("cloud-network.png"));
        stage.setTitle(msg);
        stage.setResizable(false);
        InsertDialogController insertDialogController = loader.getController();
        insertDialogController.dwc = dwc;
        stage.showAndWait();
    }
    public void createLocalDir (String dirName) {
        currentDir = currentDir.resolve(dirName);
        currentDir.toFile().mkdir();
        currentDir = currentDir.getParent();
        try {
            refreshClientView();

        } catch (IOException e) {
            log.debug("debug: ", e);
            statusUpdater("Error making local directory");
        }
        statusUpdater("A new directory has been added");
    }
    protected void mkServerDirMSG(String dirName) {
        try {
            messHandler.getOs().writeObject(new MkDirRequest(dirName));
            messHandler.getOs().flush();
        } catch (IOException e) {
            statusUpdater("Error making directory to server");
        }
    }
}
