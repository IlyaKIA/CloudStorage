import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SignInController implements Initializable {

    @FXML
    public Button signUp_butt;

    @FXML
    private PasswordField passw_field;

    @FXML
    private Button login_butt;

    @FXML
    private TextField login_field;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        login_butt.setOnAction(event -> {
            String loginText = login_field.getText().trim();
            String passText = passw_field.getText().trim();
            if(!loginText.equals("") && !passText.equals("")) loginUser(loginText, passText);
        });
        signUp_butt.setOnAction(event -> {
            login_butt.getScene().getWindow().hide();
            stageSignUp().showAndWait();
        });

    }

    private void loginUser(String loginText, String passText) {
    }

    private Stage stageSignUp (){
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("SignUp.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        return stage;
    }
}
