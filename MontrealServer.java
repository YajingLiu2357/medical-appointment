import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MontrealServer extends UnicastRemoteObject implements AppointmentInterface{
    private Map<String, Map<String, Integer>> appointmentOuter;
    private List<String> recordList;
    protected MontrealServer() throws RemoteException {
        appointmentOuter = new HashMap<>();
        recordList = new LinkedList<>();
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
    public String listAppointmentAvailability(String appointmentType) throws RemoteException {
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        //TODO get other server's appointment availability
        Map<String, Integer> appointmentAll = new HashMap<>();
        if(appointmentInner != null){
            appointmentAll.putAll(appointmentInner);
        }
        Map<String, Integer> otherAppointment = getOtherAppointment(appointmentType);
        if(otherAppointment != null){
            appointmentAll.putAll(otherAppointment);
        }
        if(appointmentAll != null){
            log = time + " List appointment availability. Request parameters: " + appointmentType + " Request: success " + "Response: " + appointmentAll.toString();
        }else{
            log = time + " List appointment availability. Request parameters: " + appointmentType + " Request: success " + "Response: fail because appointment type does not exist";
        }
        writeLog(log);
        return log;
    }

    @Override
    public String bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException {
        //TODO book appointment not same day, three for other city
        //TODO book appointment from other city
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        if(appointmentInner != null && appointmentInner.containsKey(appointmentID) && appointmentInner.get(appointmentID) > 0){
            appointmentInner.put(appointmentID, appointmentInner.get(appointmentID) - 1);
            String bookRecord = patientID + " " + appointmentID + " " + appointmentType;
            recordList.add(bookRecord);
            log = time + " Book appointment. Request parameters: " + bookRecord + " Request: success " + "Response: success";
        }else{
            log = time + " Book appointment. Request parameters: " + patientID + " " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because appointment does not exist or no capacity";
        }
        writeLog(log);
        return log;
    }

    @Override
    public String getAppointmentSchedule(String patientID) throws RemoteException {
        //TODO get other server's appointment schedule
        String time = getTime();
        List<String> schedule = new LinkedList<>();
        for (String record : recordList){
            String [] recordSplit = record.split(" ");
            if(recordSplit[0].equals(patientID)){
                schedule.add(record);
            }
        }
        String log = "";
        if(schedule.size() > 0){
            log = time + " Get appointment schedule. Request parameters: " + patientID + " Request: success " + "Response: " + schedule.toString();
        }else{
            log = time + " Get appointment schedule. Request parameters: " + patientID + " Request: success " + "Response: fail because patient has no appointment";
        }
        writeLog(log);
        return log;
    }

    @Override
    public String cancelAppointment(String patientID, String appointmentID) throws RemoteException {
        // TODO cancel appointment from other city
        String time = getTime();
        String log = "";
        for (String record : recordList){
            String [] recordSplit = record.split(" ");
            if(recordSplit[0].equals(patientID) && recordSplit[1].equals(appointmentID)){
                recordList.remove(record);
                appointmentOuter.get(recordSplit[2]).put(appointmentID, appointmentOuter.get(recordSplit[2]).get(appointmentID) + 1);
                log = time + " Cancel appointment. Request parameters: " + patientID + " " + appointmentID + " Request: success " + "Response: success";
            }else{
                log = time + " Cancel appointment. Request parameters: " + patientID + " " + appointmentID + " Request: success " + "Response: fail because appointment does not exist";
            }
        }
        writeLog(log);
        return log;
    }

    @Override
    public Map<String, Map<String, Integer>> getAppointmentOuter() throws RemoteException {
        return null;
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
    public Map<String, Integer> getOtherAppointment(String appointmentType){
        Map<String, Integer> appointmentOther = new HashMap<>();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            byte[] sendBuffer = appointmentType.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address,5001);
            socket.send(sendPacket);
            // buffer length may need to expand
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (!receiveData.equals("Not available")){
               String receiveDataTrim = receiveData.replaceAll("[{}\\s]", "");
               String [] appointments = receiveDataTrim.split(",");
               for (String appointment : appointments){
                   String [] appointmentSplit = appointment.split("=");
                   appointmentOther.put(appointmentSplit[0], Integer.parseInt(appointmentSplit[1]));
               }
            }
            return appointmentOther;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(socket != null){
                socket.close();
            }
        }
    }
}
