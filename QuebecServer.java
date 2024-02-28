
import DHMSApp.DHMSPOA;
import rmi.AppointmentInterface;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuebecServer extends DHMSPOA {
    private Map<String, Map<String, Integer>> appointmentOuter;
    private List<String> recordList;
    private List<String> recordOtherCities;
    protected QuebecServer(){
        appointmentOuter = new HashMap<>();
        recordList = new ArrayList<>();
        recordOtherCities = new ArrayList<>();
    }

    @Override
    public String hello() {
        return "Quebec Server says hello!";
    }

    @Override
    public String addAppointment(String appointmentID, String appointmentType, int capacity) {
        return null;
    }

    @Override
    public String removeAppointment(String appointmentID, String appointmentType) {
        return null;
    }

    @Override
    public String listAppointmentAvailability(String appointmentType) {
        return null;
    }

    @Override
    public String bookAppointment(String patientID, String appointmentID, String appointmentType) {
        return null;
    }

    @Override
    public String getAppointmentSchedule(String patientID) {
        return null;
    }

    @Override
    public String cancelAppointment(String patientID, String appointmentID) {
        return null;
    }

    @Override
    public String swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType) {
        return null;
    }
    public static Map<String, Map<String, Integer>> getAppointment() {
        String filePath = "./data/appointment/Quebec.txt";
        Map <String, Map<String, Integer>> appointment = new HashMap<>();
        Map <String, Integer> physician = new HashMap<>();
        Map <String, Integer> surgeon = new HashMap<>();
        Map <String, Integer> dental = new HashMap<>();
        appointment.put("Physician", physician);
        appointment.put("Surgeon", surgeon);
        appointment.put("Dental", dental);
        try{
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                String [] lineSplit = line.split(" ");
                String appointmentType = lineSplit[0];
                String appointmentID = lineSplit[1];
                int capacity = Integer.parseInt(lineSplit[2]);
                appointment.get(appointmentType).put(appointmentID, capacity);
            }
            bufferedReader.close();
            fileReader.close();
            return appointment;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public void changeAppointmentData(){
        String filePath = "./data/appointment/Quebec.txt";
        try{
            FileWriter fileWriter = new FileWriter(filePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (String appointmentType : appointmentOuter.keySet()){
                for (String appointmentID : appointmentOuter.get(appointmentType).keySet()){
                    bufferedWriter.write(appointmentType + " " + appointmentID + " " + appointmentOuter.get(appointmentType).get(appointmentID));
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.close();
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void changeRecordData(){
        String filePath = "./data/record/Quebec.txt";
        try{
            FileWriter fileWriter = new FileWriter(filePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (String record : recordList){
                bufferedWriter.write(record);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static List<String> getRecordList(){
        String filePath = "./data/record/Quebec.txt";
        List<String> recordList = new ArrayList<>();
        try{
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                recordList.add(line);
            }
            bufferedReader.close();
            fileReader.close();
            return recordList;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args) throws SocketException {
        DatagramSocket socket = new DatagramSocket(Integer.parseInt("5001"));
        DatagramSocket socketRecord = new DatagramSocket(Integer.parseInt("5004"));
        try{
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket;
            byte[] receiveDataRecord = new byte[1024];
            DatagramPacket receivePacketRecord;
            while(true){
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                InetAddress address = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String appointmentType = new String(receivePacket.getData(), 0, receivePacket.getLength());
                Map<String, Integer> appointment;
                if (receivePacket.getData() != null){
                    appointment = getAppointment().get(appointmentType);
                    String reply = "";
                    if(appointment == null){
                        reply = "Not available";
                    }else{
                        reply = appointment.toString();
                    }
                    DatagramPacket replyPacket = new DatagramPacket(reply.getBytes(), reply.length(), address, port);
                    socket.send(replyPacket);
                }
                receivePacketRecord = new DatagramPacket(receiveDataRecord, receiveDataRecord.length);
                socketRecord.receive(receivePacketRecord);
                InetAddress addressRecord = receivePacketRecord.getAddress();
                int portRecord = receivePacketRecord.getPort();
                List<String> recordList;
                if (addressRecord != null){
                    recordList = getRecordList();
                    String replyRecord = "";
                    if(recordList.size() == 0){
                        replyRecord = "Not available";
                    }else{
                        replyRecord = recordList.toString();
                    }
                    DatagramPacket replyPacketRecord = new DatagramPacket(replyRecord.getBytes(), replyRecord.length(), addressRecord, portRecord);
                    socketRecord.send(replyPacketRecord);
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        } finally{
            if(socket != null){
                socket.close();
            }
        }
    }

}
