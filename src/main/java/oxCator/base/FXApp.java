package oxCator.base;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/gui.fxml"));
        Scene scene = new Scene(root, 900, 640);
        primaryStage.setTitle("AlliGoogle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
