import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public interface Welcome extends Remote {
    boolean performUpload(String fileName, byte[] fileByteArray) throws RemoteException, ExecutionException, InterruptedException;
    byte[] performDownload(String fileName) throws RemoteException, ExecutionException, InterruptedException;
    boolean performDelete(String fileName) throws RemoteException, ExecutionException, InterruptedException;
    boolean performRename(String fileName, String newFileName) throws RemoteException, ExecutionException, InterruptedException;
    ArrayList<File> displayFiles() throws RemoteException, ExecutionException, InterruptedException;
}