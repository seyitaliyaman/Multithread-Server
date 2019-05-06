package Server;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Main extends Application {


    private TextArea textArea=new TextArea();
    private int clientNo = 0;
    private Scene scene;
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream inputFromClient;
    private DataOutputStream outputToClient;
    private String rootDirectory= "C:/Users/HP";

    @Override
    public void start(Stage primaryStage){
        scene = new Scene(new ScrollPane(textArea),450,200);
        primaryStage.setTitle("SERVER");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(9000);
                    textArea.appendText("Server started at "+new Date()+"\n");

                    while (true){
                        socket = serverSocket.accept();
                        clientNo++;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                InetAddress inet = socket.getInetAddress();
                                textArea.appendText("Client "+clientNo+"'s host name is "+inet.getHostName()+"\n");
                                textArea.appendText("Client "+clientNo+"'s IP Address is "+inet.getHostAddress()+"\n");
                            }
                        });

                        new Thread(new HandleClient(socket)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class HandleClient implements Runnable{
        private Socket socket;

        HandleClient(Socket socket){
            this.socket=socket;
        }

        @Override
        public void run() {
            try {
                inputFromClient = new DataInputStream(socket.getInputStream());
                outputToClient = new DataOutputStream(socket.getOutputStream());

                while (true){
                    String command = inputFromClient.readUTF();
                    String cmds[] = command.split(" ");
                    System.out.println(cmds.length);
                    //System.out.println(cmds[1]);
                    String cmdsnd = "";
                    for(int i=1; i<cmds.length; i++){
                        cmdsnd+=cmds[i]+" ";
                    }
                    System.out.println(cmdsnd);


                    switch (cmds[0]){
                        case "ls":
                            outputToClient.writeUTF(commandLS(rootDirectory.trim()));
                            break;
                        case "cd":
                            commandCD(cmdsnd);
                            outputToClient.writeUTF("Directory has changed!");
                            break;
                        case "mkdir":
                            if(commandMKDIR(cmdsnd)){
                                outputToClient.writeUTF("Directory has created!");
                            }else{
                                outputToClient.writeUTF("Directory already exist!");
                            }

                            break;
                        case "pwd":
                            outputToClient.writeUTF(rootDirectory);
                            break;
                        case "rm":

                            if(commandRM(cmdsnd)){
                                outputToClient.writeUTF("Directory has remove!");
                            }else{
                                outputToClient.writeUTF("Directory already removed!");
                            }

                            break;
                        case "write":



                            if(commandWRITE(cmds[1],cmds[2])){
                                outputToClient.writeUTF("Directory has moved!");
                            }else{
                                outputToClient.writeUTF("Directory has already moved!!");
                            }
                            break;
                        case "read":

                            break;


                        default:

                            outputToClient.writeUTF("Command doesn't exist!!!");
                            break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** ls, cd, mkdir, pwd, rm, write, read **/



    public String commandLS (String directory){
        String output = "";
        List<String> fileNames = new ArrayList<>();
        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory));
            for(Path path : directoryStream){
                fileNames.add(path.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String name: fileNames){
            output+=name.substring(directory.length()+1)+"\n";
        }
        return output;
    }

    public void commandCD (String directoryName){
        rootDirectory=directoryName;

    }

    public boolean commandMKDIR (String directoryName){

        //boolean create=false;
        Path path = Paths.get(rootDirectory.trim()+"\\"+directoryName.trim());
        if(!Files.exists((path))){
            try {
                Files.createDirectories(path);
                //create=true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }else{
            return false;
        }

       /* if(create){
            return true;
        }else{
            return false;
        }*/
    }


    public boolean commandRM(String fileName){
        Path path = Paths.get(rootDirectory.trim()+"\\"+fileName.trim());
        if(Files.exists((path))){
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }else{
            return false;
        }
    }

    public boolean commandWRITE(String sourceFileName,String destFileName){
        Path sourcePath = Paths.get(sourceFileName);
        Path destinationPath = Paths.get(destFileName);

        if(Files.exists(destinationPath)){
            try {
                Files.move(sourcePath,destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }else{
            return false;
        }

    }

    public void commandREAD(String sourceFileName,String destFileName){

    }



    public static void main(String[] args) {
        launch(args);
    }
}
