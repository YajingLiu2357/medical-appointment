import DHMSApp.DHMSPOA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    public String hello() {
        return "Hello, world!";
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
            log = time + " Add appointment. Request parameters: " + appointmentID + " " + appointmentType + " " + capacity + " Request: success " + "Response: success";
        }else{
            appointmentInner.put(appointmentID, capacity);
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
//        Map<String, Integer> otherAppointment = getOtherAppointment(appointmentType);
//        if(otherAppointment != null){
//            appointmentAll.putAll(otherAppointment);
//        }
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
            return null;
        }
    }

    @Override
    public String getAppointmentSchedule(String patientID) {
        // TODO: get other cities' appointment schedule
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
        // TODO: get other cities' record list
        List<String> recordListAll = new LinkedList<>();
        recordListAll.addAll(recordList);
//        Registry registry = LocateRegistry.getRegistry(1099);
//        rmi.AppointmentInterface quebecServer = (rmi.AppointmentInterface) registry.lookup("QUE");
//        List<String> recordListQuebec = quebecServer.getRecordList();
//        rmi.AppointmentInterface sherbrookeServer = (rmi.AppointmentInterface) registry.lookup("SHE");
//        List<String> recordListSherbrooke = sherbrookeServer.getRecordList();
//        if (recordListQuebec != null){
//            recordListAll.addAll(recordListQuebec);
//        }
//        if (recordListSherbrooke != null){
//            recordListAll.addAll(recordListSherbrooke);
//        }
        return recordListAll;
    }
}
