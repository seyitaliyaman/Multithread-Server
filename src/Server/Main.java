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

        Path path = Paths.get("./ServerFile");
        if(!Files.exists((path))) {
            try {
                System.out.println("var ki");
                Files.createDirectories(path);
                //create=true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        new Thread(()->{

                try {
                    ServerSocket serverSocket = new ServerSocket(9000);
                    textArea.appendText("Server started at "+new Date()+"\n");

                    while (true){
                        Socket socket = serverSocket.accept();
                        clientNo++;
                        int client = clientNo;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                InetAddress inet = socket.getInetAddress();
                                textArea.appendText("Client "+client+"'s host name is "+inet.getHostName()+"\n");
                                textArea.appendText("Client "+client+"'s IP Address is "+inet.getHostAddress()+"\n");
                            }
                        });

                        new Thread(new HandleClient(socket,client)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }).start();
    }

    class HandleClient implements Runnable{
        private Socket socket;
        private String rootDirectory;
        private int client;
        public HandleClient(Socket socket,int client){
            this.socket=socket;
            this.client=client;
            this.rootDirectory= "./ServerFile";//"C:\\Users\\HP\\Desktop\\Ders Notlarım\\2. Sınıf\\2.Dönem\\OOPWorks\\CMD Project\\Server";
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
                            textArea.appendText("Client "+client+" listed directory.\n");
                            break;
                        case "cd":

                            if(commandCD(cmdsnd)){
                                outputToClient.writeUTF("Directory has changed!\n");
                                textArea.appendText("Client "+client+" is in "+rootDirectory+"\n");
                            }else{
                                outputToClient.writeUTF("Can not change root directory\n");
                                textArea.appendText("Client "+client+" could not change directory.\n");
                            }
                            /*if(cmdsnd.contains("C:\\Users\\HP\\Desktop\\Ders Notlarım\\2. Sınıf\\2.Dönem\\OOPWorks\\CMD Project\\Server")){

                            }else{
                                commandCD(cmdsnd);

                            }*/

                            break;
                        case "mkdir":
                            if(commandMKDIR(cmdsnd,client)){
                                outputToClient.writeUTF("Directory has created!\n");
                            }else{
                                outputToClient.writeUTF("Directory already exist!\n");
                            }

                            break;
                        case "pwd":
                            outputToClient.writeUTF(rootDirectory);
                            break;
                        case "rm":

                            if(commandRM(cmdsnd,client)){
                                outputToClient.writeUTF("Directory has removed!\n");
                            }else{
                                outputToClient.writeUTF("Directory already removed!\n");
                            }

                            break;
                        case "write":



                            if(commandWRITE(cmds[1],cmds[2],client)){
                                outputToClient.writeUTF("Directory has copied to Server!\n");
                            }else{
                                outputToClient.writeUTF("Could not copied!\n");
                            }
                            break;
                        case "read":

                            if(commandREAD(cmds[1],cmds[2],client)){
                                outputToClient.writeUTF("Directory has copied to Client!\n");
                            }else{
                                outputToClient.writeUTF("Could not copied!\n");
                            }
                            break;


                        default:
                            outputToClient.writeUTF("Command doesn't exist!!!\n");
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
            if(directoryName.contains("./ServerFile")){
                Path path = Paths.get(directoryName.trim());
                if(Files.exists(path)){
                    rootDirectory = directoryName;
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }

        }

        public boolean commandMKDIR (String directoryName,int clientNo){

            //boolean create=false;
            Path path = Paths.get(rootDirectory.trim()+"/"+directoryName.trim());
            if(!Files.exists((path))){
                try {
                    Files.createDirectories(path);
                    //create=true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                textArea.appendText("Client"+clientNo+" created directory "+directoryName);
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


        public boolean commandRM(String fileName,int clientNo){
            Path path = Paths.get(rootDirectory.trim()+"/"+fileName.trim());
            if(Files.exists((path))){
                try {
                    Files.delete(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                textArea.appendText("Client"+clientNo+" deleted "+fileName);
                return true;
            }else{
                return false;
            }
        }

        public boolean commandWRITE(String sourceFileName,String destFileName, int clientNo){
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
            if (Files.exists(destinationPath)){
                textArea.appendText("Client"+clientNo+" uploaded "+destFileName);
            }
            return Files.exists(destinationPath);

        }

        public boolean commandREAD(String sourceFileName, String destFileName ,int clientNo){
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

            if (Files.exists(destinationPath)){
                textArea.appendText("Client"+clientNo+" downloaded "+destFileName);
            }

            return Files.exists(destinationPath);
        }
    }

    /** ls, cd, mkdir, pwd, rm, write, read **/







    public static void main(String[] args) {
        launch(args);
    }
}