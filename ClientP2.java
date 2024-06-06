import java.io.File;
import java.nio.file.NoSuchFileException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class ClientP2 {
    public static final String LOCALHOST = "localhost";
    public static final String clientFilesDirectory = "../client_files";

    public static ArrayList<File> displayFiles(){
        ArrayList<File> listOfFiles = new ArrayList<>();
        File file = new File(clientFilesDirectory);

        for(File currentFile : Objects.requireNonNull(file.listFiles(), "client files list returned null")){
            if(currentFile.isFile())
                listOfFiles.add(currentFile);
        }
        return listOfFiles;
    }

    public static void main(String[] args) {
        int interval = 15;  //seconds
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        try{
            Registry registry = LocateRegistry.getRegistry(LOCALHOST);
            Welcome welcome = (Welcome) registry.lookup("Welcome");

            //server syncs with client. i.e. server will be made equal to client by reflecting changes happening on client over to server.
            while (true){
                Date LAST_CHECK = new Date();
                for(int i=interval-1;i>=0;i--){
                    System.out.printf("Waiting for changes...[%02d]\r", i);
                    Thread.sleep(1000);
                }

                System.out.println("\nLooking for changes...");
                ArrayList<File> client_files = displayFiles();

                //Should capture: create new, update existing
                for (File file : client_files){
                    if(sdf.parse(sdf.format(file.lastModified())).after(LAST_CHECK)){
                        String filePath = clientFilesDirectory.concat("/").concat(file.getName());
                        byte[] fileByteArray = Files.readAllBytes(Path.of(filePath));

                        if (welcome.performUpload(file.getName(), fileByteArray)){
                            System.out.println("File Uploaded Successfully: " + file.getName());
                        }else{
                            System.out.println("File Upload Failed: " + file.getName());
                        }
                    }
                }

                //Should capture: delete
                ArrayList<String> client_files_names = (ArrayList<String>) client_files.stream().map(File::getName).collect(Collectors.toList());
                ArrayList<String> server_files_names = (ArrayList<String>) welcome.displayFiles().stream().map(File::getName).collect(Collectors.toList());

                for(String sfile : server_files_names){
                    if(!client_files_names.contains(sfile)){
                        if(welcome.performDelete(sfile)){
                            System.out.println("File Deleted Successfully: " + sfile);
                        }else {
                            System.out.println("File Deletion Failed: " + sfile);
                        }
                    }
                }

                //Should capture: rename
                server_files_names = (ArrayList<String>) welcome.displayFiles().stream().map(File::getName).collect(Collectors.toList());

                if(client_files_names.size() > server_files_names.size()){
                    for(String cfile: client_files_names){
                        if(!server_files_names.contains(cfile)){
                            String filePath = clientFilesDirectory.concat("/").concat(cfile);
                            byte[] fileByteArray = Files.readAllBytes(Path.of(filePath));

                            if (welcome.performUpload(cfile, fileByteArray)){
                                System.out.println("File Uploaded Successfully: " + cfile);
                            }else{
                                System.out.println("File Upload Failed: " + cfile);
                            }
                        }
                    }
                }
            }
        } catch (RemoteException re){
            System.out.println("EXCEPTION: RemoteException");
            re.printStackTrace();
        } catch (NoSuchFileException fe){
            System.out.println("EXCEPTION: NoSuchFileException");
            fe.printStackTrace();
        } catch (Exception e){
            System.out.println("EXCEPTION");
            e.printStackTrace();
        }
    }
}
