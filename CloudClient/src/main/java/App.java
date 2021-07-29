import UsersDB.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;



public class App extends Application {
    public static ClientMessageHandler messHandler = new ClientMessageHandler(); //TODO: is it right?
    public static User user = new User();  //TODO: is it right?
    public static Stage signInStage;  //TODO: is it right?
    public static Stage signUpStage;  //TODO: is it right?
    public static FXMLLoader loader;  //TODO: is it right?

    @Override
    public void start(Stage primaryStage) throws Exception {
        App.signInStage = primaryStage;
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SignIn.fxml")));
        primaryStage.setTitle("Cloud storage");
        primaryStage.getIcons().add(new Image("cloud-network.png"));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
