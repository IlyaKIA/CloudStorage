import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class InsertDialogController {

    @FXML
    public TextField textField;
    protected DialogWinCommand dwc;

    @FXML
    public void oKBut(ActionEvent actionEvent) {
        ClientController clientController = App.loader.getController();
        switch (dwc){
            case CREATE_LOCAL_DIR:
                clientController.createLocalDir(textField.getText());
                break;
            case CREATE_SERVER_DIR:
                clientController.mkServerDirMSG(textField.getText());
                break;
        }

        textField.getScene().getWindow().hide();
    }

    @FXML
    public void cancelBut(ActionEvent actionEvent) {
        textField.getScene().getWindow().hide();
    }
}
