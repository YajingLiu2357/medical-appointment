package rmi;


public class PatientClient extends Client {
    public PatientClient(String ID) throws Exception {
        super(ID);
    }
    public void bookAppointment(String patientID, String appointmentID, String appointmentType) throws Exception{
        String log = server.bookAppointment(patientID, appointmentID, appointmentType);
        writeLog(log);
    }
    public void getAppointmentSchedule(String patientID) throws Exception{
        String log = server.getAppointmentSchedule(patientID);
        writeLog(log);
    }
    public void cancelAppointment(String patientID, String appointmentID) throws Exception{
        String log = server.cancelAppointment(patientID, appointmentID);
        writeLog(log);
    }
    public void addAppointment(String appointmentID, String appointmentType, int capacity){
        printInvalidCommandMessage();
    }
    public void removeAppointment(String appointmentID, String appointmentType){
        printInvalidCommandMessage();
    }
    public void listAppointmentAvailability(String appointmentType){
        printInvalidCommandMessage();
    }
}
