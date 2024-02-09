import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface AppointmentInterface extends Remote{
    // Admin
    String addAppointment(String appointmentID, String appointmentType, int capacity) throws RemoteException;
    String removeAppointment(String appointmentID, String appointmentType) throws RemoteException;
    String listAppointmentAvailability (String appointmentType) throws RemoteException;
    // Patient
    String bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException;
    String getAppointmentSchedule(String patientID) throws RemoteException;
    String cancelAppointment(String patientID, String appointmentID) throws RemoteException;
    Map<String, Map<String, Integer>> getAppointmentOuter() throws RemoteException;
}
