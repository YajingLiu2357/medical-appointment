
import DHMSApp.DHMS;
import DHMSApp.DHMSHelper;
import DHMSApp.DHMSPOA;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class QuebecServer extends DHMSPOA {
    private Map<String, Map<String, Integer>> appointmentOuter;
    private List<String> recordList;
    private List<String> recordOtherCities;
    protected QuebecServer(){
        appointmentOuter = new HashMap<>();
        recordList = new LinkedList<>();
        recordOtherCities = new LinkedList<>();
        changeAppointmentData();
        changeRecordData();
    }

    @Override
    public String addAppointment(String appointmentID, String appointmentType, int capacity) {
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        if (appointmentInner != null && appointmentInner.containsKey(appointmentID)){
            log = time + Constants.ADD_APPOINTMENT + Constants.REQUEST_PARAMETERS + appointmentID + Constants.SPACE + appointmentType + Constants.SPACE + capacity + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.APPOINTMENT_ALREADY_EXISTS;
        }else{
            if (appointmentInner == null){
                appointmentInner = new HashMap<>();
                appointmentInner.put(appointmentID, capacity);
                appointmentOuter.put(appointmentType, appointmentInner);
            }
            else{
                appointmentInner.put(appointmentID, capacity);
            }
            changeAppointmentData();
            log = time + Constants.ADD_APPOINTMENT + Constants.REQUEST_PARAMETERS + appointmentID + Constants.SPACE + appointmentType + Constants.SPACE + capacity + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.SUCCESS;
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
                String [] recordSplit = record.split(Constants.SPACE);
                if(recordSplit[1].equals(appointmentID)){
                    String patientID = recordSplit[0];
                    String response = getNextAppointment(appointmentType, appointmentID);
                    if (!response.equals(Constants.NOT_AVAILABLE)){
                        recordList.remove(record);
                        log = bookAppointment(patientID, response, appointmentType);
                        appointmentInner.remove(appointmentID);
                        changeAppointmentData();
                        log = log + Constants.NEW_LINE + time + Constants.REMOVE_APPOINTMENT + Constants.REQUEST_PARAMETERS + appointmentID + Constants.SPACE + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.SUCCESS;
                        if (appointmentInner.size() == 0){
                            appointmentOuter.remove(appointmentType);
                        }
                        changeAppointmentData();
                        writeLog(log);
                        return log;
                    }else{
                        log = time + Constants.REMOVE_APPOINTMENT + Constants.REQUEST_PARAMETERS + appointmentID + Constants.SPACE + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.NO_NEXT_APPOINTMENT;
                        writeLog(log);
                        return log;
                    }
                }
            }
            appointmentInner.remove(appointmentID);
            changeAppointmentData();
            log = time + Constants.REMOVE_APPOINTMENT + Constants.REQUEST_PARAMETERS + appointmentID + Constants.SPACE + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.SUCCESS;
            if (appointmentInner.size() == 0){
                appointmentOuter.remove(appointmentType);
            }
            changeAppointmentData();
        }else{
            log = time + Constants.REMOVE_APPOINTMENT + Constants.REQUEST_PARAMETERS + appointmentID + Constants.SPACE + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.APPOINTMENT_NOT_EXIST;
        }
        writeLog(log);
        return log;
    }

    @Override
    public String listAppointmentAvailability(String appointmentType) {
        String time = getTime();
        Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
        String log = "";
        Map<String, Integer> appointmentAll = new HashMap<>();
        if(appointmentInner != null){
            appointmentAll.putAll(appointmentInner);
        }
        Map<String, Integer> mtlAppointment = getOtherAppointment(appointmentType, Constants.MTL_APPOINTMENT_PORT);
        if(mtlAppointment != null){
            appointmentAll.putAll(mtlAppointment);
        }
        Map<String, Integer> sheAppointment = getOtherAppointment(appointmentType, Constants.SHE_APPOINTMENT_PORT);
        if(sheAppointment != null){
            appointmentAll.putAll(sheAppointment);
        }
        if(appointmentAll.size()!=0){
            log = time + Constants.LIST_APPOINTMENT_AVAILABILITY + Constants.REQUEST_PARAMETERS + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + appointmentAll.toString();
        }else{
            log = time + Constants.LIST_APPOINTMENT_AVAILABILITY + Constants.REQUEST_PARAMETERS + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.APPOINTMENT_TYPE_NOT_EXIST;
        }
        writeLog(log);
        return log;
    }

    @Override
    public String bookAppointment(String patientID, String appointmentID, String appointmentType) {
        if (appointmentID.startsWith(Constants.QUE)){
            String time = getTime();
            Map<String, Integer> appointmentInner = appointmentOuter.get(appointmentType);
            String log = "";
            List<String>  recordAllList = getAllRecordList();
            if(appointmentInner != null && appointmentInner.containsKey(appointmentID) && appointmentInner.get(appointmentID) > 0){
                for (String record : recordAllList){
                    String [] recordSplit = record.split(Constants.SPACE);
                    if(recordSplit[0].equals(patientID) && recordSplit[1].equals(appointmentID)){
                        log = time + Constants.BOOK_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + appointmentID + Constants.SPACE + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.SAME_APPOINTMENT;
                        writeLog(log);
                        return log;
                    }
                    if(recordSplit[0].equals(patientID) && recordSplit[2].equals(appointmentType) && recordSplit[1].substring(4,10).equals(appointmentID.substring(4,10))){
                        log = time + Constants.BOOK_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + appointmentID + Constants.SPACE + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.HAVE_SAME_TYPE_APPOINTMENT_SAME_DAY;
                        writeLog(log);
                        return log;
                    }
                    if(!recordSplit[0].substring(0, 3).equals(recordSplit[1].substring(0,3))){
                        recordOtherCities.add(record);
                    }
                }
                if (recordOtherCities.size() > 3){
                    if (checkThreeOtherAppointment(patientID)){
                        log = time + Constants.BOOK_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + appointmentID + Constants.SPACE  + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.THREE_APPOINTMENTS_OTHER_CITIES;
                        writeLog(log);
                        return log;
                    }
                }
                synchronized (this){
                    appointmentInner.put(appointmentID, appointmentInner.get(appointmentID) - 1);
                    String bookRecord = patientID + Constants.SPACE + appointmentID + Constants.SPACE + appointmentType;
                    recordList.add(bookRecord);
                    log = time + Constants.BOOK_APPOINTMENT + Constants.REQUEST_PARAMETERS + bookRecord + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.SUCCESS;
                    changeAppointmentData();
                    changeRecordData();
                }
            }else{
                log = time + Constants.BOOK_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + appointmentID + Constants.SPACE + appointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.NO_CAPACITY;
            }
            writeLog(log);
            return log;
        }else{
            String serverName = appointmentID.substring(0,3);
            if (serverName.equals(Constants.MTL)){
                return bookCancelOtherAppointment(patientID, appointmentID, appointmentType, Constants.MTL_BOOK_CANCEL_PORT, Constants.BOOK);
            }else if (serverName.equals(Constants.SHE)){
                return bookCancelOtherAppointment(patientID, appointmentID, appointmentType, Constants.SHE_BOOK_CANCEL_PORT, Constants.BOOK);
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
            String [] recordSplit = record.split(Constants.SPACE);
            if(recordSplit[0].equals(patientID)){
                schedule.add(record);
            }
        }
        String log = "";
        if(schedule.size() > 0){
            log = time + Constants.GET_APPOINTMENT_SCHEDULE + Constants.REQUEST_PARAMETERS + patientID + Constants.REQUEST_SUCCESS + Constants.RESPONSE + schedule.toString();
        }else{
            log = time + Constants.GET_APPOINTMENT_SCHEDULE + Constants.REQUEST_PARAMETERS + patientID + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.NO_APPOINTMENT;
        }
        writeLog(log);
        return log;
    }

    @Override
    public String cancelAppointment(String patientID, String appointmentID) {
        if (appointmentID.startsWith(Constants.QUE)){
            String time = getTime();
            String log = "";
            for (String record : recordList){
                String [] recordSplit = record.split(" ");
                if(recordSplit[0].equals(patientID) && recordSplit[1].equals(appointmentID)){
                    recordList.remove(record);
                    appointmentOuter.get(recordSplit[2]).put(appointmentID, appointmentOuter.get(recordSplit[2]).get(appointmentID) + 1);
                    log = time + Constants.CANCEL_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + appointmentID + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.SUCCESS;
                    changeAppointmentData();
                    changeRecordData();
                    writeLog(log);
                    return log;
                }
            }
            log = time + Constants.CANCEL_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + appointmentID + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.APPOINTMENT_NOT_EXIST;
            writeLog(log);
            return log;
        }else{
            String serverName = appointmentID.substring(0,3);
            if (serverName.equals(Constants.MTL)){
                return bookCancelOtherAppointment(patientID, appointmentID, null, Constants.MTL_BOOK_CANCEL_PORT, Constants.CANCEL);
            }else if (serverName.equals(Constants.SHE)){
                return bookCancelOtherAppointment(patientID, appointmentID, null, Constants.SHE_BOOK_CANCEL_PORT, Constants.CANCEL);
            }
            return null;
        }
    }

    @Override
    public String swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType) {
        boolean oldAppointmentExist = false;
        boolean newAppointmentAvailable = false;
        String time = getTime();
        String log = "";
        for (String record : recordList){
            String [] recordSplit = record.split(Constants.SPACE);
            if (recordSplit[0].equals(patientID) && recordSplit[1].equals(oldAppointmentID) && recordSplit[2].equals(oldAppointmentType)){
                oldAppointmentExist = true;
                break;
            }
        }
        if (!oldAppointmentExist){
            log = time + Constants.SWAP_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + oldAppointmentID + Constants.SPACE + oldAppointmentType + Constants.SPACE + newAppointmentID + Constants.SPACE + newAppointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.APPOINTMENT_NOT_EXIST;
            writeLog(log);
            return log;
        }
        String serverName = newAppointmentID.substring(0,3);
        if (serverName.equals(Constants.MTL)){
            Map<String, Integer> queAppointment = getOtherAppointment(newAppointmentType, Constants.MTL_APPOINTMENT_PORT);
            if (queAppointment.size() != 0 && queAppointment.containsKey(newAppointmentID) && queAppointment.get(newAppointmentID) > 0){
                newAppointmentAvailable = true;
            }
        }else if (serverName.equals(Constants.SHE)){
            Map<String, Integer> sheAppointment = getOtherAppointment(newAppointmentType, Constants.SHE_APPOINTMENT_PORT);
            if (sheAppointment.size() != 0 && sheAppointment.containsKey(newAppointmentID) && sheAppointment.get(newAppointmentID) > 0){
                newAppointmentAvailable = true;
            }
        }
        if (!newAppointmentAvailable){
            log = time + Constants.SWAP_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + oldAppointmentID + Constants.SPACE + oldAppointmentType + Constants.SPACE + newAppointmentID + Constants.SPACE + newAppointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.NO_CAPACITY;
            writeLog(log);
            return log;
        }
        cancelAppointment(patientID, oldAppointmentID);
        bookAppointment(patientID, newAppointmentID, newAppointmentType);
        log = time + Constants.SWAP_APPOINTMENT + Constants.REQUEST_PARAMETERS + patientID + Constants.SPACE + oldAppointmentID + Constants.SPACE + oldAppointmentType + Constants.SPACE + newAppointmentID + Constants.SPACE + newAppointmentType + Constants.REQUEST_SUCCESS + Constants.RESPONSE + Constants.SUCCESS;
        writeLog(log);
        return log;
    }
    public static Map<String, Map<String, Integer>> getAppointment() {
        String filePath = Constants.DATA_APPOINTMENT + Constants.QUEBEC_TXT;
        Map <String, Map<String, Integer>> appointment = new HashMap<>();
        Map <String, Integer> physician = new HashMap<>();
        Map <String, Integer> surgeon = new HashMap<>();
        Map <String, Integer> dental = new HashMap<>();
        appointment.put(Constants.PHYSICIAN, physician);
        appointment.put(Constants.SURGEON, surgeon);
        appointment.put(Constants.DENTAL, dental);
        try{
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null && !line.equals("")){
                String [] lineSplit = line.split(Constants.SPACE);
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
        String filePath = Constants.DATA_APPOINTMENT + Constants.QUEBEC_TXT;
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
    public void changeRecordData(){
        String filePath = Constants.DATA_RECORD + Constants.QUEBEC_TXT;
        try{
            PrintWriter writer = new PrintWriter(filePath);
            writer.print("");
            writer.close();
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
        String filePath = Constants.DATA_RECORD + Constants.QUEBEC_TXT;
        List<String> recordList = new ArrayList<>();
        try{
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null && !line.equals("")){
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
        String path = Constants.LOG_FILE_PATH + Constants.MONTREAL_TXT;
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
        List<String> recordListAll = new LinkedList<>();
        if (recordList != null && recordList.size() > 0){
            recordListAll.addAll(recordList);
        }
        List<String> mtlRecord = getOtherRecord(Constants.MTL_RECORD_PORT);
        if (mtlRecord != null && mtlRecord.size() > 0){
            recordListAll.addAll(mtlRecord);
        }
        List<String> sheRecord = getOtherRecord(Constants.SHE_RECORD_PORT);
        if (sheRecord != null && sheRecord.size() > 0){
            recordListAll.addAll(sheRecord);
        }
        return recordListAll;
    }
    public String getTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.TIME_FORMAT);
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
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
                    return Constants.NOT_AVAILABLE;
                }
            }
        }
        return Constants.NOT_AVAILABLE;
    }
    public Map<String, Integer> getOtherAppointment(String appointmentType, String portNum){
        Map<String, Integer> appointmentOther = new HashMap<>();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(Constants.LOCALHOST);
            byte[] sendBuffer = appointmentType.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, Integer.parseInt(portNum));
            socket.send(sendPacket);
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (!receiveData.equals(Constants.NOT_AVAILABLE)){
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
    public String bookCancelOtherAppointment (String patientID, String appointmentID, String appointmentType, String portNum, String bookCancel){
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(Constants.LOCALHOST);
            String sendData = bookCancel + Constants.SPACE + patientID + Constants.SPACE + appointmentID + Constants.SPACE + appointmentType;
            byte[] sendBuffer = sendData.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, Integer.parseInt(portNum));
            socket.send(sendPacket);
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (!receiveData.equals(Constants.NOT_AVAILABLE)) {
                return receiveData;
            }
        } catch(IOException e){
            e.printStackTrace();
        } finally{
            if(socket != null){
                socket.close();
            }
        }
        return null;
    }
    public boolean checkThreeOtherAppointment(String patientID){
        int earliestDay = Integer.parseInt(recordOtherCities.get(0).split(Constants.SPACE)[1].substring(4,6));
        int earliestMonth = Integer.parseInt(recordOtherCities.get(0).split(Constants.SPACE)[1].substring(6,8));
        int earliestYear = 2000 + Integer.parseInt(recordOtherCities.get(0).split(Constants.SPACE)[1].substring(8,10));
        Calendar earliestDate = Calendar.getInstance();
        earliestDate.set(earliestYear, earliestMonth - 1 , earliestDay, 0, 0);
        for (String record : recordOtherCities){
            String [] recordSplit = record.split(Constants.SPACE);
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
            String [] recordSplit = record.split(Constants.SPACE);
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
            return true;
        }
        return false;
    }
    public List<String> getOtherRecord(String portNum){
        List<String> recordListOther = new LinkedList<>();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(Constants.LOCALHOST);
            byte[] sendBuffer = Constants.RECORD.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, Integer.parseInt(portNum));
            socket.send(sendPacket);
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (!receiveData.equals(Constants.NOT_AVAILABLE)) {
                String receiveDataTrim = receiveData.replaceAll("\\[", "").replaceAll("\\]", "");
                String [] records = receiveDataTrim.split(",");
                for (String record : records){
                    recordListOther.add(record.replaceFirst("^\\s*", ""));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(socket != null){
                socket.close();
            }
        }
        return recordListOther;
    }
    public static void main(String[] args) throws SocketException {
        ReplyAppointment replyAppointment = new ReplyAppointment(Constants.QUE_APPOINTMENT_PORT);
        replyAppointment.start();
        ReplyRecord replyRecord = new ReplyRecord(Constants.QUE_RECORD_PORT);
        replyRecord.start();
        DatagramSocket socket = new DatagramSocket(Integer.parseInt(Constants.QUE_BOOK_CANCEL_PORT));
        try{
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket;
            while(true){
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                InetAddress addressBook = receivePacket.getAddress();
                int portBook = receivePacket.getPort();
                String [] bookData = new String(receivePacket.getData(),0, receivePacket.getLength()).split(Constants.SPACE);
                String bookCancel = bookData[0];
                String patientID = bookData[1];
                String appointmentID = bookData[2];
                ORB orb = ORB.init(args, null);
                org.omg.CORBA.Object objRef = orb.resolve_initial_references(Constants.NAME_SERVICE);
                NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
                DHMS href = DHMSHelper.narrow(ncRef.resolve_str(Constants.QUE));
                if (bookCancel.equals(Constants.BOOK)){
                    String appointmentType = bookData[3];
                    String log = href.bookAppointment(patientID, appointmentID, appointmentType);
                    DatagramPacket replyPacketBook = new DatagramPacket(log.getBytes(), log.length(), addressBook, portBook);
                    socket.send(replyPacketBook);
                }else if (bookCancel.equals(Constants.CANCEL)){
                    String log = href.cancelAppointment(patientID, appointmentID);
                    DatagramPacket replyPacketBook = new DatagramPacket(log.getBytes(), log.length(), addressBook, portBook);
                    socket.send(replyPacketBook);
                }
            }
        } catch(IOException | InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName |
                NotFound e){
            e.printStackTrace();
        }
    }
}
