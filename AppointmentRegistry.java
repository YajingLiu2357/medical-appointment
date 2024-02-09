import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AppointmentRegistry {
    public static void main(String[] args) throws Exception{
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("MTL", new MontrealServer());
        registry.rebind("QUE", new QuebecServer());
    }
}
