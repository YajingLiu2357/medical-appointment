import DHMSApp.DHMS;
import DHMSApp.DHMSHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.Scanner;

public class Driver {
    static DHMS hrefMTL;
    static DHMS hrefQUE;
    static DHMS hrefSHE;
    static Client client;
    public static void enterUserID (){
        System.out.println("Please enter your ID:");
        Scanner scanner = new Scanner(System.in);
        String ID = scanner.nextLine();
        boolean validID = checkUserID(ID);
        while (!validID){
            System.out.println("Please enter your ID:");
            ID = scanner.nextLine();
            validID = checkUserID(ID);
        }
        String serverName = ID.substring(0, 3);
        char userType = ID.charAt(3);
        if (userType == 'A'){
            if (serverName.equals(Constants.MTL)){
                client = new Admin(ID, hrefMTL);
            }else if (serverName.equals(Constants.QUE)){
                client = new Admin(ID, hrefQUE);
            }else{
                client = new Admin(ID, hrefSHE);
            }
            System.out.println("Admin functions: addAppointment, removeAppointment, listAppointmentAvailability, bookAppointment, getAppointmentSchedule, cancelAppointment, swapAppointment, changeUser");
        }else {
            if (serverName.equals(Constants.MTL)){
                client = new Patient(ID, hrefMTL);
            }else if (serverName.equals(Constants.QUE)){
                client = new Patient(ID, hrefQUE);
            }else {
                client = new Patient(ID, hrefSHE);
            }
            System.out.println("Patient functions: bookAppointment, getAppointmentSchedule, cancelAppointment, swapAppointment, changeUser");
        }
    }
    public static void enterCommand (){
        System.out.println("Please enter your command:");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        String [] commandSplit = command.split(" ");
        boolean validCommand = checkCommand(commandSplit);
        while (!validCommand){
            System.out.println("Please enter your command:");
            command = scanner.nextLine();
            commandSplit = command.split(" ");
            validCommand = checkCommand(commandSplit);
        }
        String mainCommand = commandSplit[0];
        switch (mainCommand){
            case "addAppointment":
                client.addAppointment(commandSplit[1], commandSplit[2], Integer.parseInt(commandSplit[3]));
                break;
            case "removeAppointment":
                client.removeAppointment(commandSplit[1], commandSplit[2]);
                break;
            case "listAppointmentAvailability":
                client.listAppointmentAvailability(commandSplit[1]);
                break;
            case "bookAppointment":
                client.bookAppointment(commandSplit[1], commandSplit[2], commandSplit[3]);
                break;
            case "getAppointmentSchedule":
                client.getAppointmentSchedule(commandSplit[1]);
                break;
            case "cancelAppointment":
                client.cancelAppointment(commandSplit[1], commandSplit[2]);
                break;
            case "swapAppointment":
                client.swapAppointment(commandSplit[1], commandSplit[2], commandSplit[3], commandSplit[4], commandSplit[5]);
                break;
            case "changeUser":
                enterUserID();
                break;
        }
    }
    public static boolean checkUserID (String ID){
        if (ID.length() == 8){
            String serverName = ID.substring(0, 3);
            char userType = ID.charAt(3);
            if (checkServerName(serverName) && (userType == 'A' || userType == 'P')){
                return true;
            }
        }
        System.out.println("Invalid user ID. Please try again.");
        return false;
    }
    public static boolean checkServerName (String serverName){
        if (serverName.equals(Constants.MTL) || serverName.equals(Constants.QUE) || serverName.equals(Constants.SHE)){
            return true;
        }else{
            return false;
        }
    }
    public static boolean checkAppointmentID (String appointmentID){
        if (appointmentID.length() == 10){
            String serverName = appointmentID.substring(0, 3);
            char time = appointmentID.charAt(3);
            String date = appointmentID.substring(4, 10);
            if (checkServerName(serverName) && (time == 'M' || time == 'A' || time == 'E') && date.matches("^[0-9]+$")){
                return true;
            }
        }
        System.out.println("Invalid appointment ID. Please try again.");
        return false;
    }
    public static boolean checkAppointmentType (String appointmentType){
        if (appointmentType.equals("Physician") || appointmentType.equals("Surgeon") || appointmentType.equals("Dental")){
            return true;
        }
        System.out.println("Invalid appointment type. Please try again.");
        return false;
    }
    public static boolean checkCommand (String [] commandSplit){
        String mainCommand = commandSplit[0];
        switch (mainCommand){
            case "addAppointment":
                if (commandSplit.length == 4 && (checkAppointmentID(commandSplit[1]) && checkAppointmentType(commandSplit[2]) && commandSplit[3].matches("^[0-9]+$"))){
                    return true;
                }
                break;
            case "removeAppointment":
                if (commandSplit.length == 3 && (checkAppointmentID(commandSplit[1]) && checkAppointmentType(commandSplit[2]))){
                    return true;
                }
                break;
            case "listAppointmentAvailability":
                if (commandSplit.length == 2 && (checkAppointmentType(commandSplit[1]))){
                    return true;
                }
                break;
            case "bookAppointment":
                if (commandSplit.length == 4 && (checkUserID(commandSplit[1]) && checkAppointmentID(commandSplit[2]) && checkAppointmentType(commandSplit[3]))){
                    return true;
                }
                break;
            case "getAppointmentSchedule":
                if (commandSplit.length == 2 && (checkUserID(commandSplit[1]))){
                    return true;
                }
                break;
            case "cancelAppointment":
                if (commandSplit.length == 3 && (checkUserID(commandSplit[1]) && checkAppointmentID(commandSplit[2]))){
                    return true;
                }
                break;
            case "swapAppointment":
                if (commandSplit.length == 6 && (checkUserID(commandSplit[1]) && checkAppointmentID(commandSplit[2]) && checkAppointmentType(commandSplit[3]) && checkAppointmentID(commandSplit[4]) && checkAppointmentType(commandSplit[5]))){
                    return true;
                }
                break;
            case "changeUser":
                return true;
            default:
                System.out.println("Invalid command. Please try again.");
                return false;
        }
        System.out.println("Invalid command. Please try again.");
        return false;
    }
    public static void main (String args[]){
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Constants.NAME_SERVICE);
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            hrefMTL = DHMSHelper.narrow(ncRef.resolve_str(Constants.MTL));
            hrefQUE = DHMSHelper.narrow(ncRef.resolve_str(Constants.QUE));
            hrefSHE = DHMSHelper.narrow(ncRef.resolve_str(Constants.SHE));
            enterUserID();
            while(true){
                enterCommand();
            }
        }catch (InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName | NotFound e) {
            e.printStackTrace();
        }
    }
}
