
import DHMSApp.DHMS;
import DHMSApp.DHMSHelper;
import DHMSApp.DHMSPOA;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import rmi.AppointmentInterface;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        return null;
    }

    @Override
    public String listAppointmentAvailability(String appointmentType) {
        return null;
    }

    @Override
    public String bookAppointment(String patientID, String appointmentID, String appointmentType) {
        if (appointmentID.startsWith("QUE")){
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
//            Registry registry = LocateRegistry.getRegistry(1099);
//            String serverName = appointmentID.substring(0,3);
//            AppointmentInterface server = (AppointmentInterface) registry.lookup(serverName);
//            String log = server.bookAppointment(patientID, appointmentID, appointmentType);
//            writeLog(log);
            return null;
        }
    }

    @Override
    public String getAppointmentSchedule(String patientID) {
        return null;
    }

    @Override
    public String cancelAppointment(String patientID, String appointmentID) {
        if (appointmentID.startsWith("QUE")){
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
        }else {
            // TODO: cancel appointment from other cities
//            Registry registry = LocateRegistry.getRegistry(1099);
//            String serverName = appointmentID.substring(0,3);
//            rmi.AppointmentInterface server = (rmi.AppointmentInterface) registry.lookup(serverName);
//            String log = server.cancelAppointment(patientID, appointmentID);
//            writeLog(log);
//            return log;
            return null;
        }
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
    public void writeLog(String log){
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
    public List<String> getAllRecordList(){
        // TODO: get MTL record list
        List<String> recordListAll = new LinkedList<>();
        if (recordList != null){
            recordListAll.addAll(recordList);
        }
//        DatagramSocket socketMTL = null;
//        DatagramSocket socketSHE = null;
//        try {
////            socketMTL = new DatagramSocket();
////            InetAddress address = InetAddress.getByName("localhost");
////            byte[] sendBuffer = new byte[1024];
////            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, 5006);
////            socketMTL.send(sendPacket);
////            // buffer length may need to expand
////            byte[] receiveBuffer = new byte[1024];
////            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
////            socketMTL.receive(receivePacket);
////            String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
////            if (!receiveData.equals("Not available")) {
////                String receiveDataTrim = receiveData.replaceAll("\\[", "").replaceAll("\\]", "");
////                String [] records = receiveDataTrim.split(",");
////                for (String record : records){
////                    recordListAll.add(record);
////                }
////            }
////            socketSHE = new DatagramSocket();
//            InetAddress address = InetAddress.getByName("localhost");
//            byte[] sendBuffer = "record".getBytes();
//            byte[] receiveBuffer = new byte[1024];
//            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address,5005);
//            socketSHE.send(sendPacket);
//            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//            socketSHE.receive(receivePacket);
//            String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
//            if (!receiveData.equals("Not available")) {
//                String receiveDataTrim = receiveData.replaceAll("\\[", "").replaceAll("\\]", "");
//                String [] records = receiveDataTrim.split(",");
//                for (String record : records){
//                    recordListAll.add(record);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            if(socketMTL != null){
//                socketMTL.close();
//            }
//            if(socketSHE != null){
//                socketSHE.close();
//            }
//        }
        return recordListAll;
    }
    public String getTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
    public static void main(String[] args) throws SocketException {
        ReplyAppointment replyAppointment = new ReplyAppointment("5001");
        replyAppointment.start();
        ReplyRecord replyRecord = new ReplyRecord("5004");
        replyRecord.start();
        DatagramSocket socket = new DatagramSocket(Integer.parseInt("5007"));
        try{
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket;
            while(true){
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                InetAddress addressBook = receivePacket.getAddress();
                int portBook = receivePacket.getPort();
                String [] bookData = new String(receivePacket.getData(),0, receivePacket.getLength()).split(" ");
                String bookCancel = bookData[0];
                if (bookCancel.equals("book")){
                    String patientID = bookData[1];
                    String appointmentID = bookData[2];
                    String appointmentType = bookData[3];
                    ORB orb = ORB.init(args, null);
                    org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
                    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
                    DHMS href = DHMSHelper.narrow(ncRef.resolve_str("QUE"));
                    String log = href.bookAppointment(patientID, appointmentID, appointmentType);
                    DatagramPacket replyPacketBook = new DatagramPacket(log.getBytes(), log.length(), addressBook, portBook);
                    socket.send(replyPacketBook);
                }else if (bookCancel.equals("cancel")){
                    String patientID = bookData[1];
                    String appointmentID = bookData[2];
                    ORB orb = ORB.init(args, null);
                    org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
                    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
                    DHMS href = DHMSHelper.narrow(ncRef.resolve_str("QUE"));
                    String log = href.cancelAppointment(patientID, appointmentID);
                    DatagramPacket replyPacketBook = new DatagramPacket(log.getBytes(), log.length(), addressBook, portBook);
                    socket.send(replyPacketBook);
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        } catch (InvalidName invalidName) {
            invalidName.printStackTrace();
        } catch (CannotProceed cannotProceed) {
            cannotProceed.printStackTrace();
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
            invalidName.printStackTrace();
        } catch (NotFound notFound) {
            notFound.printStackTrace();
        }
    }
}
