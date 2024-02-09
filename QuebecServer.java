import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class QuebecServer extends UnicastRemoteObject implements AppointmentInterface{
    private Map<String, Map<String, Integer>> appointmentOuter;

    protected QuebecServer() throws RemoteException {
        appointmentOuter = new HashMap<>();
    }

    @Override
    public String addAppointment(String appointmentID, String appointmentType, int capacity) throws RemoteException {
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        if(appointmentInner == null){
            appointmentInner = new HashMap<>();
            appointmentInner.put(appointmentID, capacity);
            appointmentOuter.put(appointmentType, appointmentInner);
            log = time + " Add appointment. Request parameters: " + appointmentID + " " + appointmentType + " " + capacity + " Request: success " + "Response: success";
        }else{
            log = time + " Add appointment. Request parameters: " + appointmentID + " " + appointmentType + " " + capacity + " Request: success " + "Response: fail because appointment type already exists";
        }
        writeLog(log);
        return log;
    }

    @Override
    public String removeAppointment(String appointmentID, String appointmentType) throws RemoteException {
        return null;
    }

    @Override
    public String listAppointmentAvailability(String appointmentType) throws RemoteException {
        return null;
    }

    @Override
    public String bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException {
        return null;
    }

    @Override
    public String getAppointmentSchedule(String patientID) throws RemoteException {
        return null;
    }

    @Override
    public String cancelAppointment(String patientID, String appointmentID) throws RemoteException {
        return null;
    }
    public String getTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
    public static void writeLog(String log){
        String path = "./logs/server/Quebec.txt";
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

    public Map<String, Map<String, Integer>> getAppointmentOuter() {
        return appointmentOuter;
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        AppointmentInterface quebecServer = (AppointmentInterface) registry.lookup("QUE");
        DatagramSocket socket = new DatagramSocket(Integer.parseInt("5001"));
        try{
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket;
            while(true){
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                InetAddress address = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String appointmentType = new String(receivePacket.getData(), 0, receivePacket.getLength());
                Map<String, Integer> appointment;
                if (receivePacket.getData() != null){
                    appointment =quebecServer.getAppointmentOuter().get(appointmentType);
                    String reply = "";
                    if(appointment == null){
                        reply = "Not available";
                    }else{
                        reply = appointment.toString();
                    }
                    DatagramPacket replyPacket = new DatagramPacket(reply.getBytes(), reply.length(), address, port);
                    socket.send(replyPacket);
                }
            }
        } catch(IOException e){
            throw new RuntimeException(e);
        } finally{
            if(socket != null){
                socket.close();
            }
        }
    }
}
