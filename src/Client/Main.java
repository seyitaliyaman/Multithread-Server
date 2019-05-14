package Client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main extends Application {

    private DataInputStream fromServer = null;
    private DataOutputStream toServer = null;
    private TextField textField;
    private TextArea textArea;
    private BorderPane paneForTF,mainPane;
    private Scene scene;
    private Label label = new Label();

    private Socket socket;

    @Override
    public void start(Stage primaryStage){
        label.setText("Command:");
        paneForTF = new BorderPane();
        paneForTF.setPadding(new Insets(5,5,5,5));
        paneForTF.setStyle("-fx-border-color: green");
        paneForTF.setLeft(label);

        textField = new TextField();
        textField.setAlignment(Pos.BOTTOM_LEFT);



        paneForTF.setCenter(textField);

        mainPane = new BorderPane();

        textArea=new TextArea();
        textArea.appendText("Server Folder is: ./ServerFile\nClient Folder is: ./ClientFolder\n");
        textArea.setEditable(false);

        mainPane.setCenter(new ScrollPane(textArea));
        mainPane.setTop(paneForTF);

        scene = new Scene(mainPane,450,200);
        primaryStage.setTitle("CLIENT");
        primaryStage.setScene(scene);
        primaryStage.show();

        Path path = Paths.get("./ClientFolder");
        if(!Files.exists((path))) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            socket = new Socket("localhost",9000);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            textArea.appendText(e.getMessage());
        }

        textField.setOnAction(new ServerSenderHandler());
    }

    class ServerSenderHandler implements EventHandler<ActionEvent>{

        @Override
        public void handle(ActionEvent event) {

            String command = textField.getText();

            System.out.println("giden "+command);
            textField.setText("");
            try {
                toServer.writeUTF(command);
                textArea.appendText("Executed Command: "+command+"\n");
                toServer.flush();
                String response = fromServer.readUTF();
                textArea.appendText(" "+response+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
