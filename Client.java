
import DHMSApp.DHMS;
import DHMSApp.DHMSHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class Client {

    public static void main(String args[]) {
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            DHMS href = DHMSHelper.narrow(ncRef.resolve_str("MTL-SERVER"));
            DHMS href2 = DHMSHelper.narrow(ncRef.resolve_str("QUE-SERVER"));
            String hello = href2.hello();
            System.out.println(hello);

            String result = href.addAppointment("MTLA270224", "Physician", 4);
            //String result2 = href.removeAppointment("MTLA270224", "Physician");
            String result3 = href.listAppointmentAvailability("Physician");
            String result4 = href.bookAppointment("MTLP0001", "MTLA270224", "Physician");
//            String result5 = href.getAppointmentSchedule("MTLP0001");
//            String result6 = href.cancelAppointment("MTLP0001", "MTLA270224");
            System.out.println(result3);
        } catch (InvalidName invalidName) {
            invalidName.printStackTrace();
        } catch (CannotProceed cannotProceed) {
            cannotProceed.printStackTrace();
        } catch (org.omg.CosNaming.NamingContextPackage.InvalidName invalidName) {
            invalidName.printStackTrace();
        } catch (NotFound notFound) {
            notFound.printStackTrace();
        }

    }

}