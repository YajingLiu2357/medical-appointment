import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface AppointmentInterface extends Remote{
    // Admin
    String addAppointment(String appointmentID, String appointmentType, int capacity) throws RemoteException;
    String removeAppointment(String appointmentID, String appointmentType) throws RemoteException, NotBoundException;
    String listAppointmentAvailability (String appointmentType) throws RemoteException, NotBoundException;
    // Patient
    String bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException, NotBoundException;
    String getAppointmentSchedule(String patientID) throws RemoteException, NotBoundException;
    String cancelAppointment(String patientID, String appointmentID) throws RemoteException, NotBoundException;
    Map<String, Map<String, Integer>> getAppointmentOuter() throws RemoteException;
    List<String> getRecordList() throws RemoteException;
}
