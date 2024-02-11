import java.util.Scanner;

public class Driver {
    public static void main(String[] args) throws Exception{
//        AdminClient adminClientMTL = new AdminClient("MTLA0001");
//        adminClientMTL.addAppointment("MTLA080224", "Physician", 2);
//        AdminClient adminClientQUE = new AdminClient("QUEA0001");
//        adminClientQUE.addAppointment("QUEA080224", "Physician", 2);
//        adminClientQUE.addAppointment("QUEM080224", "Surgeon", 2);
//        adminClientMTL.listAppointmentAvailability("Physician");
//        //adminMTL.removeAppointment("MTLA080224", "Physician");
//        PatientClient patientClientMTL = new PatientClient("MTLP0001");
//        patientClientMTL.bookAppointment("MTLP0001", "MTLA080224", "Physician");
//        patientClientMTL.getAppointmentSchedule("MTLP0001");
//        patientClientMTL.cancelAppointment("MTLP0001", "MTLA080224");

        System.out.println("Please enter your ID:");
        Scanner scanner = new Scanner(System.in);
        String ID = scanner.nextLine();
        char userType = ID.charAt(3);
        Client client;
        if (userType == 'A'){
            client = new AdminClient(ID);
            System.out.println("Admin functions: addAppointment, removeAppointment, listAppointmentAvailability, changeUser");
        }else {
            client = new PatientClient(ID);
            System.out.println("Patient functions: bookAppointment, getAppointmentSchedule, cancelAppointment, changeUser");
        }
        while(true){
            System.out.println("Please enter your command:");
            String command = scanner.nextLine();
            String [] commandSplit = command.split(" ");
            if (commandSplit[0].equals("addAppointment")){
                client.addAppointment(commandSplit[1], commandSplit[2], Integer.parseInt(commandSplit[3]));
            }else if (commandSplit[0].equals("removeAppointment")){
                client.removeAppointment(commandSplit[1], commandSplit[2]);
            }else if (commandSplit[0].equals("listAppointmentAvailability")){
                client.listAppointmentAvailability(commandSplit[1]);
            }else if (commandSplit[0].equals("bookAppointment")){
                client.bookAppointment(commandSplit[1], commandSplit[2], commandSplit[3]);
            }else if (commandSplit[0].equals("getAppointmentSchedule")){
                client.getAppointmentSchedule(commandSplit[1]);
            }else if (commandSplit[0].equals("cancelAppointment")){
                client.cancelAppointment(commandSplit[1], commandSplit[2]);
            }else if (commandSplit[0].equals("changeUser")){
                System.out.println("Please enter your ID:");
                ID = scanner.nextLine();
                userType = ID.charAt(3);
                if (userType == 'A'){
                    client = new AdminClient(ID);
                    System.out.println("Admin functions: addAppointment, removeAppointment, listAppointmentAvailability, changeUser");
                }else {
                    client = new PatientClient(ID);
                    System.out.println("Patient functions: bookAppointment, getAppointmentSchedule, cancelAppointment, changeUser");
                }
            }else{
                System.out.println("Invalid command");
            }
        }

    }
}
