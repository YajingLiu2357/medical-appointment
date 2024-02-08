import java.rmi.Remote;
import java.rmi.RemoteException;
public interface AppointmentInterface extends Remote{
    // Admin
    String addAppointment(String appointmentID, String appointmentType, int capacity) throws RemoteException;
    String removeAppointment(String appointmentID, String appointmentType) throws RemoteException;
    void listAppointmentAvailability (String appointmentType) throws RemoteException;
    // Patient
    void bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException;
    void getAppointmentSchedule(String patientID) throws RemoteException;
    void cancelAppointment(String patientID, String appointmentID) throws RemoteException;
    // Test
    void test() throws RemoteException;

}
