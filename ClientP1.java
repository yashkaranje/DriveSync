import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.NoSuchFileException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientP1 {
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
        try{
            Scanner sc = new Scanner(System.in);
            int ch;

            Registry registry = LocateRegistry.getRegistry(LOCALHOST);
            Welcome welcome = (Welcome) registry.lookup("Welcome");

            do {
                System.out.println("""
                        Choose:
                         1. Upload
                         2. Download
                         3. Delete
                         4. Rename
                         5. Display
                         0. Exit""");
                ch = Integer.parseInt(sc.nextLine());

                switch (ch) {
                    case 1:
                        try {
                            System.out.println("Enter the text file name to upload from client's directory.");
                            System.out.println("Available files: " + Objects.requireNonNull(displayFiles(), "displayFiles returned null").stream().map(File::getName).toList());
                            String fileName = sc.nextLine().split("[ ]")[0];
                            String filePath = clientFilesDirectory.concat("/").concat(fileName);
                            byte[] fileByteArray = Files.readAllBytes(Path.of(filePath));

                            if (welcome.performUpload(fileName, fileByteArray)){
                                System.out.println("File Uploaded Successfully: " + fileName);
                            }else{
                                System.out.println("File Upload Failed: " + fileName);
                            }
                        } catch (NoSuchFileException fe){
                            System.out.println("EXCEPTION: NoSuchFileException");
                            fe.printStackTrace();
                        } catch (Exception e){
                            System.out.println("EXCEPTION");
                            e.printStackTrace();
                        }
                        break;

                    case 2:
                        try {
                            System.out.println("Enter the text file name to download from server's directory.");
                            System.out.println("Available files: " + Objects.requireNonNull(welcome.displayFiles(), "displayFiles returned null").stream().map(File::getName).toList());
                            String fileName = sc.nextLine().split("[ ]")[0];
                            byte[] fileByteArray = welcome.performDownload(fileName);

                            if (fileByteArray != null){
                                FileOutputStream fileOutputStream = new FileOutputStream(clientFilesDirectory.concat("/").concat(fileName));
                                fileOutputStream.write(fileByteArray);
                                System.out.println("File Downloaded Successfully: " + fileName);
                                fileOutputStream.close();
                            }else{
                                System.out.println("File Download Failed: " + fileName);
                            }
                        } catch (Exception e){
                            System.out.println("EXCEPTION");
                            e.printStackTrace();
                        }
                        break;

                    case 3:
                        try {
                            System.out.println("Enter the text file name to delete from server's directory.");
                            System.out.println("Available files: " + Objects.requireNonNull(welcome.displayFiles(), "displayFiles returned null").stream().map(File::getName).toList());
                            String fileName = sc.nextLine().split("[ ]")[0];

                            if(welcome.performDelete(fileName)){
                                System.out.println("File Deleted Successfully: " + fileName);
                            }else {
                                System.out.println("File Deletion Failed: " + fileName);
                            }
                        }catch (Exception e){
                            System.out.println("EXCEPTION");
                            e.printStackTrace();
                        }
                        break;

                    case 4:
                        try {
                            System.out.println("Enter the text file name to rename from server's directory.");
                            System.out.println("Available files: " + Objects.requireNonNull(welcome.displayFiles(), "displayFiles returned null").stream().map(File::getName).toList());
                            String fileName = sc.nextLine().split("[ ]")[0];
                            System.out.println("Enter the new name with extension");
                            String newFileName = sc.nextLine().split("[ ]")[0];

                            if(welcome.performRename(fileName, newFileName)){
                                System.out.printf("File Renamed Successfully: %s -> %s%n", fileName, newFileName);
                            }else {
                                System.out.println("File Renaming Failed: " + fileName);
                            }
                        }catch (Exception e){
                            System.out.println("EXCEPTION");
                            e.printStackTrace();
                        }
                        break;

                    case 5:
                        try {
                            System.out.println("Available client files: " + Objects.requireNonNull(displayFiles(), "displayFiles returned null").stream().map(File::getName).toList());
                            System.out.println("Available server files: " + Objects.requireNonNull(welcome.displayFiles(), "displayFiles returned null").stream().map(File::getName).toList());
                        }catch (Exception e){
                            System.out.println("EXCEPTION");
                            e.printStackTrace();
                        }
                        break;

                    case 0: System.out.println("Aborted");
                        break;

                    default: System.out.println("Choose Wisely");
                        break;
                }
            } while(ch!=0);
        }catch (RemoteException re){
            System.out.println("EXCEPTION: RemoteException");
            re.printStackTrace();
        } catch (Exception e){
            System.out.println("EXCEPTION: Exception");
            e.printStackTrace();
        }
    }
}