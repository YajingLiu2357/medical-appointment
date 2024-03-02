import DHMSApp.DHMSPOA;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MontrealServer extends DHMSPOA {
    private Map<String, Map<String, Integer>> appointmentOuter;
    private List<String> recordList;
    private List<String> recordOtherCities;
    protected MontrealServer(){
        appointmentOuter = new HashMap<>();
        recordList = new LinkedList<>();
        recordOtherCities = new LinkedList<>();
    }

    @Override
    public String hello() {
        return "Montreal Server says hello!";
    }

    @Override
    public String addAppointment(String appointmentID, String appointmentType, int capacity) {
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        if (appointmentInner != null && appointmentInner.containsKey(appointmentID)){
            log = time + " Add appointment. Request parameters: " + appointmentID + " " + appointmentType + " " + capacity + " Request: success " + "Response: fail because appointment already exists";
        }else if (appointmentInner == null){
            appointmentInner = new HashMap<>();
            appointmentInner.put(appointmentID, capacity);
            appointmentOuter.put(appointmentType, appointmentInner);
            changeAppointmentData();
            log = time + " Add appointment. Request parameters: " + appointmentID + " " + appointmentType + " " + capacity + " Request: success " + "Response: success";
        }else{
            appointmentInner.put(appointmentID, capacity);
            changeAppointmentData();
            log = time + " Add appointment. Request parameters: " + appointmentID + " " + appointmentType + " " + capacity + " Request: success " + "Response: success";
        }
        writeLog(log);
        return log;
    }

    @Override
    public String removeAppointment(String appointmentID, String appointmentType) {
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        if(appointmentInner != null && appointmentInner.containsKey(appointmentID)){
            for (String record : recordList){
                String [] recordSplit = record.split(" ");
                if(recordSplit[1].equals(appointmentID)){
                    String patientID = recordSplit[0];
                    String response = getNextAppointment(appointmentType, appointmentID);
                    if (!response.equals("Not available")){
                        recordList.remove(record);
                        log = bookAppointment(patientID, response, appointmentType);
                        appointmentInner.remove(appointmentID);
                        changeAppointmentData();
                        log = log + "\n" + time + " Remove appointment. Request parameters: " + appointmentID + " " + appointmentType + " Request: success " + "Response: success";
                        if (appointmentInner.size() == 0){
                            appointmentOuter.remove(appointmentType);
                        }
                        writeLog(log);
                        return log;
                    }else{
                        log = time + " Remove appointment. Request parameters: " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because patient has no next available appointment";
                        writeLog(log);
                        return log;
                    }
                }
            }
            appointmentInner.remove(appointmentID);
            changeAppointmentData();
            log = time + " Remove appointment. Request parameters: " + appointmentID + " " + appointmentType + " Request: success " + "Response: success";
            if (appointmentInner.size() == 0){
                appointmentOuter.remove(appointmentType);
            }
        }else{
            log = time + " Remove appointment. Request parameters: " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because appointment does not exist";
        }
        writeLog(log);
        return log;
    }

    @Override
    public String listAppointmentAvailability(String appointmentType) {
        // TODO: get other cities' appointment availability
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        Map<String, Integer> appointmentAll = new HashMap<>();
        if(appointmentInner != null){
            appointmentAll.putAll(appointmentInner);
        }
        Map<String, Integer> otherAppointment = getOtherAppointment(appointmentType);
        if(otherAppointment != null){
            appointmentAll.putAll(otherAppointment);
        }
        if(appointmentAll.size()!=0){
            log = time + " List appointment availability. Request parameters: " + appointmentType + " Request: success " + "Response: " + appointmentAll.toString();
        }else{
            log = time + " List appointment availability. Request parameters: " + appointmentType + " Request: success " + "Response: fail because appointment type does not exist";
        }
        writeLog(log);
        return log;
    }

    @Override
    public String bookAppointment(String patientID, String appointmentID, String appointmentType) {
        if (appointmentID.startsWith("MTL")){
            String time = getTime();
            Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
            String log = "";
            List<String>  recordAllList = getAllRecordList();
            if(appointmentInner != null && appointmentInner.containsKey(appointmentID) && appointmentInner.get(appointmentID) > 0){
                for (String record : recordAllList){
                    String [] recordSplit = record.split(" ");
                    if(recordSplit[0].equals(patientID) && recordSplit[1].equals(appointmentID)){
                        log = time + " Book appointment. Request parameters: " + patientID + " " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because patient already has appointment";
                        writeLog(log);
                        return log;
                    }
                    if(recordSplit[0].equals(patientID) && recordSplit[2].equals(appointmentType) && recordSplit[1].substring(4,10).equals(appointmentID.substring(4,10))){
                        log = time + " Book appointment. Request parameters: " + patientID + " " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because patient cannot book same type of appointment on the same day";
                        writeLog(log);
                        return log;
                    }
                    if(!recordSplit[0].substring(0, 3).equals(recordSplit[1].substring(0,3))){
                        recordOtherCities.add(record);
                    }
                }
                if (recordOtherCities.size() > 3){
                    int earliestDay = Integer.parseInt(recordOtherCities.get(0).split(" ")[1].substring(4,6));
                    int earliestMonth = Integer.parseInt(recordOtherCities.get(0).split(" ")[1].substring(6,8));
                    int earliestYear = 2000 + Integer.parseInt(recordOtherCities.get(0).split(" ")[1].substring(8,10));
                    Calendar earliestDate = Calendar.getInstance();
                    earliestDate.set(earliestYear, earliestMonth - 1 , earliestDay, 0, 0);
                    for (String record : recordOtherCities){
                        String [] recordSplit = record.split(" ");
                        if (recordSplit[0].equals(patientID)){
                            int day = Integer.parseInt(recordSplit[1].substring(4,6));
                            int month = Integer.parseInt(recordSplit[1].substring(6,8));
                            int year = Integer.parseInt(recordSplit[1].substring(8,10));
                            if (year < earliestYear){
                                earliestYear = year;
                                earliestMonth = month;
                                earliestDay = day;
                            }else if (year == earliestYear && month < earliestMonth){
                                earliestMonth = month;
                                earliestDay = day;
                            }else if (year == earliestYear && month == earliestMonth && day < earliestDay){
                                earliestDay = day;
                            }
                        }
                    }
                    int count = 0;
                    for (String record : recordOtherCities){
                        String [] recordSplit = record.split(" ");
                        if (recordSplit[0].equals(patientID)){
                            int day = Integer.parseInt(recordSplit[1].substring(4,6));
                            int month = Integer.parseInt(recordSplit[1].substring(6,8));
                            int year = Integer.parseInt(recordSplit[1].substring(8,10));
                            Calendar tempDate = Calendar.getInstance();
                            tempDate.set(year, month - 1 , day, 0, 0);
                            long diff = tempDate.getTimeInMillis() - earliestDate.getTimeInMillis();
                            long diffDays = diff / (24 * 60 * 60 * 1000);
                            if (diffDays <= 7){
                                count++;
                            }
                        }
                    }
                    if (count > 3){
                        log = time + " Book appointment. Request parameters: " + patientID + " " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because patient cannot book more than 3 appointments from other cities";
                        writeLog(log);
                        return log;
                    }
                }
                synchronized (this){
                    appointmentInner.put(appointmentID, appointmentInner.get(appointmentID) - 1);
                    String bookRecord = patientID + " " + appointmentID + " " + appointmentType;
                    recordList.add(bookRecord);
                    log = time + " Book appointment. Request parameters: " + bookRecord + " Request: success " + "Response: success";
                }
            }else{
                log = time + " Book appointment. Request parameters: " + patientID + " " + appointmentID + " " + appointmentType + " Request: success " + "Response: fail because appointment does not exist or no capacity";
            }
            writeLog(log);
            return log;
        }else{
            // TODO: book appointment from other cities
//            Registry registry = LocateRegistry.getRegistry(1099);
//            String serverName = appointmentID.substring(0,3);
//            rmi.AppointmentInterface server = (rmi.AppointmentInterface) registry.lookup(serverName);
//            String log = server.bookAppointment(patientID, appointmentID, appointmentType);
//            writeLog(log);
            String serverName = appointmentID.substring(0,3);
            if (serverName.equals("QUE")){
                DatagramSocket socketQUE = null;
                try {
                    socketQUE = new DatagramSocket();
                    InetAddress address = InetAddress.getByName("localhost");
                    String sendData = "book " + patientID + " " + appointmentID + " " + appointmentType;
                    byte[] sendBuffer = sendData.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, 5007);
                    socketQUE.send(sendPacket);
                    // buffer length may need to expand
                    byte[] receiveBuffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socketQUE.receive(receivePacket);
                    String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (!receiveData.equals("Not available")) {
                        return receiveData;
                    }
                } catch(IOException e){
                    e.printStackTrace();
                } finally{
                    if(socketQUE != null){
                        socketQUE.close();
                    }
                }
            }else if (serverName.equals("SHE")){

            }
            return null;
        }
    }

    @Override
    public String getAppointmentSchedule(String patientID) {
        String time = getTime();
        List<String> schedule = new LinkedList<>();
        List<String> recordAllList = getAllRecordList();
        for (String record : recordAllList){
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
    public String cancelAppointment(String patientID, String appointmentID) {
        if (appointmentID.startsWith("MTL")){
            String time = getTime();
            String log = "";
            for (String record : recordList){
                String [] recordSplit = record.split(" ");
                if(recordSplit[0].equals(patientID) && recordSplit[1].equals(appointmentID)){
                    recordList.remove(record);
                    appointmentOuter.get(recordSplit[2]).put(appointmentID, appointmentOuter.get(recordSplit[2]).get(appointmentID) + 1);
                    log = time + " Cancel appointment. Request parameters: " + patientID + " " + appointmentID + " Request: success " + "Response: success";
                    writeLog(log);
                    return log;
                }
            }
            log = time + " Cancel appointment. Request parameters: " + patientID + " " + appointmentID + " Request: success " + "Response: fail because appointment does not exist";
            writeLog(log);
            return log;
        }else{
            // TODO: cancel appointment from other cities
//            Registry registry = LocateRegistry.getRegistry(1099);
//            String serverName = appointmentID.substring(0,3);
//            rmi.AppointmentInterface server = (rmi.AppointmentInterface) registry.lookup(serverName);
//            String log = server.cancelAppointment(patientID, appointmentID);
//            writeLog(log);
//            return log;
            String serverName = appointmentID.substring(0,3);
            if (serverName.equals("QUE")){
                DatagramSocket socketQUE = null;
                try {
                    socketQUE = new DatagramSocket();
                    InetAddress address = InetAddress.getByName("localhost");
                    String sendData = "cancel " + patientID + " " + appointmentID;
                    byte[] sendBuffer = sendData.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, 5007);
                    socketQUE.send(sendPacket);
                    // buffer length may need to expand
                    byte[] receiveBuffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socketQUE.receive(receivePacket);
                    String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (!receiveData.equals("Not available")) {
                        return receiveData;
                    }
                } catch(IOException e){
                    e.printStackTrace();
                } finally{
                    if(socketQUE != null){
                        socketQUE.close();
                    }
                }
            }else if (serverName.equals("SHE")){

            }
            return null;
        }
    }

    @Override
    public String swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType) {
        return null;
    }
    public String getTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
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
    public String getNextAppointment(String appointmentType, String appointmentID){
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        if(appointmentInner != null){
            for (String key : appointmentInner.keySet()){
                char slot = key.charAt(3);
                int day = Integer.parseInt(key.substring(4,6));
                int month = Integer.parseInt(key.substring(6,8));
                int year = Integer.parseInt(key.substring(8,10));
                char previousSlot = appointmentID.charAt(3);
                int previousDay = Integer.parseInt(appointmentID.substring(4,6));
                int previousMonth = Integer.parseInt(appointmentID.substring(6,8));
                int previousYear = Integer.parseInt(appointmentID.substring(8,10));
                if ((day == previousDay && month == previousMonth && year == previousYear) && ((previousSlot == 'M' && (slot == 'A'|| slot == 'E')||(previousSlot == 'A' && (slot == 'E'))))){
                    return key;
                }else if (day > previousDay && month == previousMonth && year == previousYear){
                    return key;
                }else if (month > previousMonth && year == previousYear){
                    return key;
                }else if (year > previousYear){
                    return key;
                }else{
                    return "Not available";
                }
            }
        }
        return "Not available";
    }
    public List<String> getAllRecordList(){
        List<String> recordListAll = new LinkedList<>();
        if (recordList != null){
            recordListAll.addAll(recordList);
        }
        DatagramSocket socketQUE = null;
        DatagramSocket socketSHE = null;
        try {
            socketQUE = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            byte[] sendBuffer = "record".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, 5004);
            System.out.println("send record 350");
            socketQUE.send(sendPacket);
            System.out.println("send record 352");
            // buffer length may need to expand
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socketQUE.receive(receivePacket);
            String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (!receiveData.equals("Not available")) {
                String receiveDataTrim = receiveData.replaceAll("\\[", "").replaceAll("\\]", "");
                String [] records = receiveDataTrim.split(",");
                for (String record : records){
                    recordListAll.add(record);
                }
            }
            socketSHE = new DatagramSocket();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address,5005);
            socketSHE.send(sendPacket);
            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socketSHE.receive(receivePacket);
            receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (!receiveData.equals("Not available")) {
                String receiveDataTrim = receiveData.replaceAll("\\[", "").replaceAll("\\]", "");
                String [] records = receiveDataTrim.split(",");
                for (String record : records){
                    recordListAll.add(record);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(socketQUE != null){
                socketQUE.close();
            }
            if(socketSHE != null){
                socketSHE.close();
            }
        }
        return recordListAll;
    }
    public static Map<String, Map<String, Integer>> getAppointment() {
        String filePath = "./data/appointment/Montreal.txt";
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
        String filePath = "./data/appointment/Montreal.txt";
        try{
            PrintWriter writer = new PrintWriter(filePath);
            writer.print("");
            writer.close();
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
    public Map<String, Integer> getOtherAppointment(String appointmentType){
        Map<String, Integer> appointmentOther = new HashMap<>();
        DatagramSocket socketQUE = null;
        DatagramSocket socketSHE = null;
        try {
            socketQUE = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            byte[] sendBuffer = appointmentType.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address,5001);
            socketQUE.send(sendPacket);
            // buffer length may need to expand
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socketQUE.receive(receivePacket);
            String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (!receiveData.equals("Not available")){
                String receiveDataTrim = receiveData.replaceAll("[{}\\s]", "");
                String [] appointments = receiveDataTrim.split(",");
                for (String appointment : appointments){
                    String [] appointmentSplit = appointment.split("=");
                    appointmentOther.put(appointmentSplit[0], Integer.parseInt(appointmentSplit[1]));
                }
            }
            socketSHE = new DatagramSocket();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address,5003);
            socketSHE.send(sendPacket);
            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socketSHE.receive(receivePacket);
            receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
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
            if(socketQUE != null){
                socketQUE.close();
            }
            if(socketSHE != null){
                socketSHE.close();
            }
        }
    }
}
