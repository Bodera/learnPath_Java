//The main class for a JavaFX application extends the javafx.application.Application class. The start() method is the main entry point for all JavaFX applications.
import javafx.application.Application;
//
import javafx.event.ActionEvent;
//Whenever a user interacts with the application (nodes), an event is said to have been occurred.
import javafx.event.EventHandler;
//A JavaFX application defines the user interface container by means of a stage and a scene. The JavaFX Stage class is the top-level JavaFX container. The JavaFX Scene class is the container for all content.
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage; 

public class HelloWord extends Application {
    /**
    * Overriding is a feature that allows a subclass or child class to provide a specific implementation of a method that is already provided by one of its super-classes or parent classes.
    */
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say to us 'Hello World!'.");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        
        Scene myScene = new Scene(root, 300, 400);
        
        primaryStage.setTitle("First application");
        primaryStage.setScene(myScene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
