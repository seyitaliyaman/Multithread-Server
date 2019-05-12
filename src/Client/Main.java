package Client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
//C:\Users\HP
    @Override
    public void start(Stage primaryStage){
        label.setText("./Server");
        paneForTF = new BorderPane();
        paneForTF.setPadding(new Insets(5,5,5,5));
        paneForTF.setStyle("-fx-border-color: green");
        paneForTF.setLeft(label);

        textField = new TextField();
        textField.setAlignment(Pos.BOTTOM_LEFT);

        paneForTF.setCenter(textField);

        mainPane = new BorderPane();
        textArea=new TextArea();
        mainPane.setCenter(new ScrollPane(textArea));
        mainPane.setTop(paneForTF);

        scene = new Scene(mainPane,450,200);
        primaryStage.setTitle("CLIENT");
        primaryStage.setScene(scene);
        primaryStage.show();

        Path path = Paths.get("./ClientFolder");
        if(!Files.exists((path))) {
            try {
                System.out.println("var ki");
                Files.createDirectories(path);
                //create=true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String cmd = "dir seyit ali dir";
        String [] kelime = null;
        kelime = cmd.split("dir");
        for(int i=0; i<kelime.length;  i++){
            System.out.println(kelime[i]);
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
                toServer.flush();
                String response = fromServer.readUTF();
                textArea.appendText(response+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
