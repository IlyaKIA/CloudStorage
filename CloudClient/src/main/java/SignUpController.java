import UsersDB.DB_Handler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML
    private PasswordField signUp_passw_field;

    @FXML
    private Button signUp_butt;

    @FXML
    private TextField signUp_login_field;

    @FXML
    private TextField name_feald;

    @FXML
    private TextField last_name_faeld;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DB_Handler dbHandler = new DB_Handler();
        signUp_butt.setOnAction(event -> {
            dbHandler.signUpUser(signUp_login_field.getText(), signUp_passw_field.getText(),
                    name_feald.getText(), last_name_faeld.getText());
        });
    }
}
