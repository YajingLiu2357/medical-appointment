import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MontrealServer extends UnicastRemoteObject implements AppointmentInterface{
    private Map<String, Map<String, Integer>> appointmentOuter = new HashMap<>();
    protected MontrealServer() throws RemoteException {
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
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        if(appointmentInner != null){
            if(appointmentInner.containsKey(appointmentID)){
                appointmentInner.remove(appointmentID);
                log = time + " Remove appointment. Request parameters: " + appointmentID + " " + appointmentType + " Request: success " + "Response: success";
            }else{
                log = time + " Remove appointment. Request parameters: " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because appointment ID does not exist";
            }
        }else{
            log = time + " Remove appointment. Request parameters: " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because appointment type does not exist";
        }
        writeLog(log);
        return log;
        // TODO If patient booked the appointment, notify the patient and book next available appointment
    }

    @Override
    public void listAppointmentAvailability(String appointmentType) throws RemoteException {

    }

    @Override
    public void bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException {

    }

    @Override
    public void getAppointmentSchedule(String patientID) throws RemoteException {

    }

    @Override
    public void cancelAppointment(String patientID, String appointmentID) throws RemoteException {

    }

    @Override
    public void test() throws RemoteException {
        System.out.println("Test");
    }
    public void writeLog (String log){
        String path = "./logs/server/Montreal.txt";
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
    public String getTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}
