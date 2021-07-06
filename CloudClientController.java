import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.paint.Color.GREEN;


public class CloudClientController implements Initializable {

    public ListView<String> localFileList;
    public Button connectButt;
    public ChoiceBox localFilePath;
    String[] localFileListItems = {"1.txt", "2.txt", "3.txt"};
    String[] localFilePathItems = {"C:\\Client\\KuchaevIA"};
    private Socket socket;
    private NetworkProcessor netProc;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        localFileList.getItems().addAll(localFileListItems);
        localFilePath.getItems().addAll(localFilePathItems);
        localFileList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println(localFileList.getSelectionModel().getSelectedItem());
            }
        });
    }

    public void process(ActionEvent actionEvent) throws IOException {
        Button button = (Button) actionEvent.getSource();
        String type = button.getText();
        switch (type) {
            case "Connect":
                System.out.println("Connection");
                socket = new Socket("localhost", 8189);
                netProc = new NetworkProcessor();
                netProc.Connect(socket, connectButt);
                break;
            case "To":
                System.out.println("To Serv");
                if(localFileList.getSelectionModel().getSelectedItem() != null && localFilePath.getSelectionModel().getSelectedItem() != null && connectButt.getTextFill() == GREEN){
                    netProc.SandFile(localFilePath.getSelectionModel().getSelectedItem() + "\\" + localFileList.getSelectionModel().getSelectedItem());
                } else {
                    System.out.println("Not connected or file not select");
                }
                break;
            case "From":
                //TODO
                break;
        }
    }


    public void exitApp(MouseEvent mouseEvent) {

    }
}
