import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    public ListView<String> localFileList;
    public Label output;
    public ListView<String>  serverFileList;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        String fileName = localFileList.getSelectionModel().getSelectedItem();
        os.writeObject(new FileMessage(Paths.get("dir", fileName)));
        os.flush();
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

    public void sendFromServer(ActionEvent actionEvent) {
    }

    public void setServerFileList(ListView<String> serverFileList) {
        this.serverFileList = serverFileList;
    }
}
