import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.*;

public class Server<T> implements Welcome, Callable<T> {
    String fileName, newFileName;
    byte[] fileByteArray;
    Operation operation;
    enum Operation { performUpload, performDownload, performDelete, performRename, displayFiles }
    public static final String serverFilesDirectory = "../server_files";
    ExecutorService executorService = Executors.newCachedThreadPool();
    T t;

    public void setT(T t) {
        this.t = t;
    }

    public T getT() {
        return t;
    }

    public Server(){}

    public Server(String fileName, byte[] fileByteArray, Operation operation) {
        this.fileName = fileName;
        this.fileByteArray = fileByteArray;
        this.operation = operation;
    }

    public Server(String fileName, Operation operation) {
        this.fileName = fileName;
        this.operation = operation;
    }

    public Server(String fileName, String newFileName, Operation operation) {
        this.fileName = fileName;
        this.newFileName = newFileName;
        this.operation = operation;
    }

    public Server(Operation operation) {
        this.operation = operation;
    }

    @Override
    public T call() {
        Server<Object> server_result;
        boolean flag;
        System.out.printf("Executing %s on %s%n", operation, Thread.currentThread().getName());

        switch (operation) {
            case performUpload -> {
                Objects.requireNonNull(fileName, "fileName is null");
                Objects.requireNonNull(fileByteArray, "fileByteArray is null");
                flag = false;
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(serverFilesDirectory.concat("/").concat(fileName));
                    fileOutputStream.write(fileByteArray);
                    System.out.println("File Uploaded Successfully: " + fileName);
                    flag = true;
                    fileOutputStream.close();
                } catch (Exception e) {
                    System.out.println("EXCEPTION");
                    e.printStackTrace();
                }
                server_result = new Server<>();
                server_result.setT(flag);
                return (T) server_result.getT();
            }

            case performDownload -> {
                Objects.requireNonNull(fileName, "fileName is null");
                byte[] fileByteArray = null;
                try {
                    String filePath = serverFilesDirectory.concat("/").concat(fileName);
                    fileByteArray = Files.readAllBytes(Path.of(filePath));
                } catch (NoSuchFileException fe){
                    System.out.println("EXCEPTION: NoSuchFileException");
                    fe.printStackTrace();
                } catch (Exception e){
                    System.out.println("EXCEPTION");
                    e.printStackTrace();
                }
                server_result = new Server<>();
                server_result.setT(fileByteArray);
                return (T) server_result.getT();
            }

            case performDelete -> {
                Objects.requireNonNull(fileName, "fileName is null");
                flag = false;
                try {
                    Files.delete(Paths.get(serverFilesDirectory.concat("/").concat(fileName)));
                    System.out.println("File Deleted Successfully: " + fileName);
                    flag = true;
                } catch (NoSuchFileException fe){
                    System.out.println("EXCEPTION: NoSuchFileException");
                    fe.printStackTrace();
                } catch (Exception e){
                    System.out.println("EXCEPTION");
                    e.printStackTrace();
                }
                server_result = new Server<>();
                server_result.setT(flag);
                return (T) server_result.getT();
            }

            case performRename -> {
                Objects.requireNonNull(fileName, "fileName is null");
                Objects.requireNonNull(newFileName, "newFileName is null");
                try{
                    File oldFile = new File(serverFilesDirectory.concat("/").concat(fileName));
                    File newFile = new File(serverFilesDirectory.concat("/").concat(newFileName));
                    server_result = new Server<>();
                    server_result.setT(oldFile.renameTo(newFile));
                    System.out.printf("File Renamed Successfully: %s -> %s%n",fileName, newFileName);
                    return (T) server_result.getT();
                } catch (Exception e){
                    System.out.println("EXCEPTION");
                    e.printStackTrace();
                }
            }

            case displayFiles -> {
                ArrayList<File> listOfFiles = new ArrayList<>();
                File file = new File(serverFilesDirectory);
                for (File currentFile : Objects.requireNonNull(file.listFiles(), "server files list returned null")) {
                    if (currentFile.isFile())
                        listOfFiles.add(currentFile);
                }
                server_result = new Server<>();
                server_result.setT(listOfFiles);
                return (T) server_result.getT();
            }

            default -> System.out.println("ERROR: Default Case");
        }
        return null;
    }

    @Override
    public boolean performUpload(String fileName, byte[] fileByteArray) throws RemoteException, ExecutionException, InterruptedException {
        Future<T> result = executorService.submit(new Server<>(fileName, fileByteArray, Operation.performUpload));
        return (Boolean) result.get();
    }

    @Override
    public byte[] performDownload(String fileName) throws RemoteException, ExecutionException, InterruptedException {
        Future<T> result = executorService.submit(new Server<>(fileName, Operation.performDownload));
        return (byte[]) result.get();
    }

    @Override
    public boolean performDelete(String fileName) throws RemoteException, ExecutionException, InterruptedException {
        Future<T> result = executorService.submit(new Server<>(fileName, Operation.performDelete));
        return (Boolean) result.get();
    }

    @Override
    public boolean performRename(String fileName, String newFileName) throws RemoteException, ExecutionException, InterruptedException {
        Future<T> result = executorService.submit(new Server<>(fileName, newFileName, Operation.performRename));
        return (Boolean) result.get();
    }

    @Override
    public ArrayList<File> displayFiles() throws RemoteException, ExecutionException, InterruptedException {
        Future<T> result = executorService.submit(new Server<>(Operation.displayFiles));
        return (ArrayList<File>) result.get();
    }

    public static void main(String[] args) {
        try {
            Server<Object> server = new Server<>();
            Welcome welcome = (Welcome) UnicastRemoteObject.exportObject(server, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Welcome", welcome);

            System.out.println("Server Ready");
        } catch (RemoteException re){
            System.out.println("EXCEPTION: RemoteException");
            re.printStackTrace();
        } catch (Exception e){
            System.out.println("EXCEPTION: Exception");
            e.printStackTrace();
        }
    }
}
