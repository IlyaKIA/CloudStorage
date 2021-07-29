import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class SignIn_Controller implements Initializable {
    @FXML
    public Button login_butt_id;
    @FXML
    public Label statusText;
    @FXML
    public Button signUp_butt;
    @FXML
    private PasswordField passw_field;
    @FXML
    private TextField login_field;

    Thread readThread;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        readThread = new Thread(() -> {
            try {
                while (!readThread.isInterrupted()) {
                    AbstractCommand command = (AbstractCommand) App.messHandler.getIs().readObject();
                    log.debug("received: {}", command);
                    switch (command.getType()) {
                        case AUTH_RESPONSE:
                            AuthenticationResponse authResponse = (AuthenticationResponse) command;
                            App.user.setLogin(authResponse.getLogin());
                            App.user.setPassword(authResponse.getPassword());
                            App.user.setFirstName(authResponse.getFirstName());
                            App.user.setLastName(authResponse.getLastName());
                            switchToCloud();
                            readThread.interrupt();
                            break;
                        case SIMPLE_MESSAGE:
                            Message message = (Message) command;
                            if (message.toString().equals("Registration successful")) {
                                Platform.runLater(() -> {
                                    App.signUpStage.hide();
                                    App.signInStage.show();
                                });
                            }
                            Platform.runLater(() -> {
                                if (statusText.getScene().getWindow().isShowing()) statusText.setText(message.toString());
                                else {
                                    SignUp_Controller signUpController = App.loader.getController();
                                    signUpController.statusTextSignUp.setText(message.toString());
                                }
                            });
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
                e.printStackTrace();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    public void SignUpButt(ActionEvent event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
        if(App.signUpStage == null) {
            App.signUpStage = newScene("SignUp.fxml", "");
        }
        App.signUpStage.show();
    }

    public void login_butt() {
        String loginText = login_field.getText().trim();
        String passText = passw_field.getText().trim();
        if(!loginText.equals("") && !passText.equals("")) loginUser(loginText, passText);
    }

    private void loginUser(String loginText, String passText) {
        try {
            App.messHandler.getOs().writeObject(new AuthenticationRequest(loginText, passText));
        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    private void switchToCloud() {
        Platform.runLater(() -> {
            try {
                readThread.join();
            } catch (InterruptedException e) {
                log.error("Error: {}", e.getMessage());
            }
            login_butt_id.getScene().getWindow().hide();
            newScene("CloudClient.fxml", App.user.getLogin()).showAndWait();
        });
    }

    private Stage newScene(String sceneName, String userName) {
        App.loader = new FXMLLoader();
        App.loader.setLocation(getClass().getResource(sceneName));
        try {
            App.loader.load();
        } catch (IOException e) {
            log.debug("Error scene loading: {}", e.getClass());
        }
        Parent root = App.loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.getIcons().add(new Image("cloud-network.png"));
        if (!userName.equals("")) stage.setTitle("Cloud storage. User: " + userName);
        else stage.setTitle("Cloud storage");
        stage.setResizable(false);
        return stage;
    }
}
