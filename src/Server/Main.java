package Server;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



//Server klasörü olacak ve onun dışına çıkmayacak
//Exceptionlar ayarlanacak (Oluşabilecek hatalarda error message dönderecek)
//Yeni client açıldığı zaman root directoryden başlayacak
//İsteğe bağlı görsel güzelleştirmeler yapılabilir
//

public class Main extends Application {


    private TextArea textArea=new TextArea();
    private int clientNo = 0;
    private Scene scene;




    @Override
    public void start(Stage primaryStage){
        scene = new Scene(new ScrollPane(textArea),450,200);
        primaryStage.setTitle("SERVER");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(()->{
                try {
                    ServerSocket serverSocket = new ServerSocket(9000);
                    textArea.appendText("Server started at "+new Date()+"\n");

                    while (true){
                        Socket socket = serverSocket.accept();
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

        }).start();
    }

    class HandleClient implements Runnable{
        private Socket socket;
        private String rootDirectory;
        public HandleClient(Socket socket){
            this.socket=socket;
            this.rootDirectory= "C:\\Users\\HP\\Desktop\\Ders Notlarım\\2. Sınıf\\2.Dönem\\OOPWorks\\CMD Project\\Server";
        }

        @Override
        public void run() {
            try {
                DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

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

                            if(commandCD(cmdsnd)){
                                outputToClient.writeUTF("Directory has changed!");
                            }else{
                                outputToClient.writeUTF("Can not change root directory");
                            }
                            /*if(cmdsnd.contains("C:\\Users\\HP\\Desktop\\Ders Notlarım\\2. Sınıf\\2.Dönem\\OOPWorks\\CMD Project\\Server")){

                            }else{
                                commandCD(cmdsnd);

                            }*/

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
                                outputToClient.writeUTF("Directory has copied!");
                            }else{
                                outputToClient.writeUTF("Directory has already copied!!");
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

        public String commandLS (String directory){
            String output = "";
            List<String> fileNames = new ArrayList<>();
            try {
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory));
                for(Path path : directoryStream){
                    fileNames.add(path.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            for(String name: fileNames){
                output+=name.substring(directory.length()+1)+"\n";
            }
            return output;
        }

        public boolean commandCD (String directoryName){
            if(directoryName.contains("C:\\Users\\HP\\Desktop\\Ders Notlarım\\2. Sınıf\\2.Dönem\\OOPWorks\\CMD Project\\Server")){
                Path path = Paths.get(directoryName.trim());
                if(Files.exists(path)){
                    rootDirectory=directoryName;
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }

        }

        public boolean commandMKDIR (String directoryName){

            //boolean create=false;
            Path path = Paths.get(rootDirectory.trim()+"\\"+directoryName.trim());
            if(!Files.exists((path))){
                try {
                    Files.createDirectories(path);
                    //create=true;
                } catch (Exception e) {
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
                } catch (Exception e) {
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

        /*if(Files.exists(destinationPath)){
            try {
                Files.copy(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }else{
            return false;
        }*/
            try {
                Files.copy(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Files.exists(destinationPath);

        }

        public boolean commandREAD(String sourceFileName, String destFileName){
            Path sourcePath = Paths.get(sourceFileName);
            Path destinationPath = Paths.get(destFileName);

        /*if(Files.exists(destinationPath)){
            try {
                Files.copy(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }else{
            return false;
        }*/
            try {
                Files.copy(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Files.exists(destinationPath);
        }
    }

    /** ls, cd, mkdir, pwd, rm, write, read **/







    public static void main(String[] args) {
        launch(args);
    }
}