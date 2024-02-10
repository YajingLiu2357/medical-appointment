import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class Client {
    String ID;
    AppointmentInterface server;
    public Client(String ID) throws Exception {
        this.ID = ID;
        getServer(ID);
    }
    public void getServer(String ID) throws Exception{
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        String serverName = ID.substring(0, 3);
        server = (AppointmentInterface) registry.lookup(serverName);
    }
    public void writeLog (String log){
        String path = "./logs/client/"+ID+".txt";
        try{
            File file = new File(path);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(path, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(log);
            bufferedWriter.newLine();
            bufferedWriter.close();
            fileWriter.close();
            System.out.println(log);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void printInvalidCommandMessage(){
        System.out.println("Invalid command. Please try again.");
    }
}
