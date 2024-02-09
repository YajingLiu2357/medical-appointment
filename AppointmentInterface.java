import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface AppointmentInterface extends Remote{
    // Admin
    String addAppointment(String appointmentID, String appointmentType, int capacity) throws RemoteException;
    String removeAppointment(String appointmentID, String appointmentType) throws RemoteException;
    String listAppointmentAvailability (String appointmentType) throws RemoteException;
    // Patient
    void bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException;
    void getAppointmentSchedule(String patientID) throws RemoteException;
    void cancelAppointment(String patientID, String appointmentID) throws RemoteException;
    Map<String, Map<String, Integer>> getAppointmentOuter() throws RemoteException;
}
