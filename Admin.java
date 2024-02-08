import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Admin extends Client{

    public Admin(String ID) throws Exception {
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
    public void listAppointmentAvailability(String appointmentType){

    }
}
