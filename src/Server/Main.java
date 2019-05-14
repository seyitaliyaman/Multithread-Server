package Server;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.*;
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

    @Override
    public void start(Stage primaryStage){
        scene = new Scene(new ScrollPane(textArea),450,200);
        primaryStage.setTitle("SERVER");
        primaryStage.setScene(scene);
        primaryStage.show();

        textArea.setEditable(false);

        Path path = Paths.get("./ServerFile");
        if(!Files.exists((path))) {
            try {

                Files.createDirectories(path);
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
        DataOutputStream outputToClient;
        DataInputStream inputFromClient;
        byte[] bytes;
        public HandleClient(Socket socket,int client){
            this.socket=socket;
            this.client=client;
            this.rootDirectory= "./ServerFile";
        }

        @Override
        public void run() {
            try {
                inputFromClient = new DataInputStream(socket.getInputStream());
                outputToClient = new DataOutputStream(socket.getOutputStream());

                while (true){
                    String command = "";

                    String cmds[];
                    System.out.println(command);
                    command = inputFromClient.readUTF();
                    cmds = command.split(" ");

                    if (cmds[0].equals("write")){
                        while(inputFromClient.available() == 0);
                        bytes = new byte[inputFromClient.available()];
                        inputFromClient.read(bytes);
                    }

                    /*try{

                        System.out.println("asd");
                    }
                    catch (UTFDataFormatException e){
                        inputFromClient.read(bytes);
                        System.out.println("lalala");
                    }*/



                    String cmdsnd = "";
                    for(int i=1; i<cmds.length; i++){
                        cmdsnd+=cmds[i]+" ";
                    }
                    System.out.println(cmdsnd);

                    System.out.println(cmds[0]);


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
                            textArea.appendText("Client "+client+"'s current directory is "+rootDirectory+"\n");
                            break;
                        case "rm":

                            if(commandRM(cmdsnd,client)){
                                outputToClient.writeUTF("Directory has removed!\n");
                            }else{
                                outputToClient.writeUTF("Directory does not exist!\n");
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

                if(Files.exists(path)&&path.toFile().isDirectory()){
                    rootDirectory = directoryName;
                    return true;
                }
                else {
                    return false;
                }
            }else{
                Path path = Paths.get(rootDirectory+"/"+directoryName.trim());
                if (Files.exists(path)&&path.toFile().isDirectory()){
                    rootDirectory = rootDirectory+"/"+directoryName;
                    return true;
                }
                return false;
            }

        }

        public boolean commandMKDIR (String directoryName,int clientNo){

            Path path = Paths.get(rootDirectory.trim()+"/"+directoryName.trim());

            if (directoryName.contains("./ServerFile")){
                path = Paths.get(directoryName.trim());
            }
            if(!Files.exists(path)){
                try {
                    Files.createDirectories(path);
                    textArea.appendText("Client "+clientNo+" created directory "+path.toString()+"\n");

                    //create=true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }else{
                textArea.appendText("Client "+clientNo+" could not created directory "+path.toString()+"\n");

                return false;
            }

        }


        public boolean commandRM(String fileName,int clientNo){
            Path path = Paths.get(rootDirectory.trim()+"/"+fileName.trim());
            if (fileName.contains("./ServerFile")){
                path = Paths.get(fileName.trim());
            }
            if(Files.exists((path))){
                try {
                    deleteDirectoryRecursion(path);
                    textArea.appendText("Client "+clientNo+" deleted "+fileName+"\n");

                }catch (DirectoryNotEmptyException e1){

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }else{
                textArea.appendText("Client "+clientNo+" could not delete "+fileName+"\n");
                return false;
            }
        }

        void deleteDirectoryRecursion(Path path) throws IOException {
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                    for (Path entry : entries) {
                        deleteDirectoryRecursion(entry);
                    }
                }
            }
            Files.delete(path);
        }


        public boolean commandWRITE(String sourceFileName,String destFileName, int clientNo){
            Path sourcePath = Paths.get(sourceFileName);
            Path destinationPath = Paths.get(destFileName);
            if (!destFileName.contains("./ServerFile")||!sourceFileName.contains("./ClientFolder")){
                return false;
            }

            try {

                //Files.copy(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);


                FileOutputStream fos = new FileOutputStream(destinationPath.toString());
                fos.write(bytes);
                System.out.println(bytes.length);
                fos.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (Files.exists(destinationPath)){
                textArea.appendText("Client "+clientNo+" uploaded "+destFileName+"\n");
            }
            return Files.exists(destinationPath);

        }

        public boolean commandREAD(String sourceFileName, String destFileName ,int clientNo){
            Path sourcePath = Paths.get(sourceFileName);
            Path destinationPath = Paths.get(destFileName);

            if (!sourceFileName.contains("./ServerFile")||!destFileName.contains("./ClientFolder")){
                return false;
            }

            try {
                File file = new File(sourcePath.toString());
                long length = file.length();
                if (length > Integer.MAX_VALUE) {
                    System.out.println("File is too large.");
                }
                byte[] bytes = new byte[(int) length];


                try {
                    FileInputStream fis = new FileInputStream(file);
                    System.out.println("b");
                    //while ((i = fis.read(bytes)) > 0) {
                    //  toServer.write(bytes, 0, i);
                    //}
                    fis.read(bytes);
                    outputToClient.write(bytes);
                    outputToClient.flush();
                    //toServer = new DataOutputStream(socket.getOutputStream());
                }catch (IOException e) {
                    e.printStackTrace();
                }
                //Files.copy(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Files.exists(destinationPath)){
                textArea.appendText("Client "+clientNo+" downloaded "+destFileName+"\n");
            }

            return Files.exists(destinationPath);
        }
    }

    /** ls, cd, mkdir, pwd, rm, write, read **/


    public static void main(String[] args) {
        launch(args);
    }
}