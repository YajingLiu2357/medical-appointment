
import DHMSApp.DHMS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class Client {
    String ID;
    DHMS server;
    public Client(String ID, DHMS server){
        this.ID = ID;
        this.server = server;
    }
    public abstract void addAppointment(String appointmentID, String appointmentType, int capacity);
    public abstract void removeAppointment(String appointmentID, String appointmentType);
    public abstract void listAppointmentAvailability(String appointmentType);
    public abstract void bookAppointment(String patientID, String appointmentID, String appointmentType);
    public abstract void getAppointmentSchedule(String patientID);
    public abstract void cancelAppointment(String patientID, String appointmentID);
    public abstract void swapAppointment(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID, String newAppointmentType);
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

//    public static void main(String args[]) {
//        try {
//            // create and initialize the ORB
//            ORB orb = ORB.init(args, null);
//            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Constants.NAME_SERVICE);
//            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
//            DHMS href = DHMSHelper.narrow(ncRef.resolve_str(Constants.MTL));
//            DHMS href2 = DHMSHelper.narrow(ncRef.resolve_str(Constants.QUE));
//            DHMS href3 = DHMSHelper.narrow(ncRef.resolve_str(Constants.SHE));
//            String result1 = href.addAppointment("MTLA270224", "Physician", 4);
//            String result = href2.addAppointment("QUEA270224", "Physician", 4);
//            //String result2 = href.removeAppointment("MTLA270224", "Physician");
////            String result3 = href2.listAppointmentAvailability("Physician");
//            String test = href.bookAppointment("MTLP0001", "MTLA270224", "Physician");
//            String result5 = href.swapAppointment("MTLP0001", "MTLA270224", "Physician", "QUEA270224", "Physician");
//            System.out.println(result5);
////            System.out.println(test);
////            String result5 = href2.getAppointmentSchedule("QUEP0001");
////            String result4 = href.bookAppointment("MTLP0001", "QUEA270224", "Physician");
////            String result7 = href2.cancelAppointment("QUEP0001", "QUEA270224");
//////            String result5 = href.getAppointmentSchedule("MTLP0001");
//////            String result6 = href.cancelAppointment("MTLP0001", "MTLA270224");
////            System.out.println(result4);
////            System.out.println(result7);
//        } catch (InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName | NotFound e) {
//            e.printStackTrace();
//        }
//
//    }

}