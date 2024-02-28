import DHMSApp.DHMSPOA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class SherbrookeServer extends DHMSPOA {
    @Override
    public String hello() {
        return null;
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
        String filePath = "./data/appointment/Sherbrooke.txt";
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
    public static void main(String[] args) throws SocketException {
        DatagramSocket socket = new DatagramSocket(Integer.parseInt("5003"));
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
