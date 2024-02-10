import java.rmi.RemoteException;

public class AdminClient extends Client{

    public AdminClient(String ID) throws Exception {
        super(ID);
    }

    public void addAppointment(String appointmentID, String appointmentType, int capacity) throws Exception{
        String log = server.addAppointment(appointmentID, appointmentType, capacity);
        writeLog(log);
    }
    public void removeAppointment(String appointmentID, String appointmentType) throws Exception {
        String log = server.removeAppointment(appointmentID, appointmentType);
        writeLog(log);
    }
    public void listAppointmentAvailability(String appointmentType) throws RemoteException {
        String log = server.listAppointmentAvailability(appointmentType);
        writeLog(log);
    }
    public void bookAppointment(String patientID, String appointmentID, String appointmentType){
        printInvalidCommandMessage();
    }
    public void getAppointmentSchedule(String patientID){
        printInvalidCommandMessage();
    }
    public void cancelAppointment(String patientID, String appointmentID){
        printInvalidCommandMessage();
    }
}
