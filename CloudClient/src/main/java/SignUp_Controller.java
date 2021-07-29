import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class SignUp_Controller {
    @FXML
    public Button signUpButtID;
    @FXML
    public PasswordField signUp_passw_field;
    @FXML
    public TextField signUp_login_field;
    @FXML
    public TextField name_feald;
    @FXML
    public TextField last_name_faeld;
    @FXML
    public Label statusTextSignUp;

    @FXML
    public void BackToSignInButt() {
        signUpButtID.getScene().getWindow().hide();
        App.signInStage.show();
    }

    @FXML
    public void RegisterUserButt() {
        if (!name_feald.getText().equals("") && !last_name_faeld.getText().equals("") &&
                !signUp_login_field.getText().equals("") && !signUp_passw_field.getText().equals("")) {
            registerUser(signUp_login_field.getText(), signUp_passw_field.getText(), name_feald.getText(), last_name_faeld.getText());
        } else {
            if(name_feald.getText().equals("")) name_feald.setStyle("-fx-background-color: RED;"); else name_feald.setStyle("-fx-background-color: WHITE;");
            if(last_name_faeld.getText().equals("")) last_name_faeld.setStyle("-fx-background-color: RED;");else last_name_faeld.setStyle("-fx-background-color: WHITE;");
            if(signUp_login_field.getText().equals("")) signUp_login_field.setStyle("-fx-background-color: RED;"); else signUp_login_field.setStyle("-fx-background-color: WHITE;");
            if(signUp_passw_field.getText().equals("")) signUp_passw_field.setStyle("-fx-background-color: RED;"); else signUp_passw_field.setStyle("-fx-background-color: WHITE;");
        }
    }

    private void registerUser(String signUp_login_field, String signUp_passw_field, String name_feald, String last_name_faeld) {
        try {
            App.messHandler.getOs().writeObject(new RegistrationRequest(signUp_login_field, signUp_passw_field, name_feald, last_name_faeld));
        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
        }
    }
}

