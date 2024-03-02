
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
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Constants.NAME_SERVICE);
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            DHMS href = DHMSHelper.narrow(ncRef.resolve_str(Constants.MTL));
            DHMS href2 = DHMSHelper.narrow(ncRef.resolve_str(Constants.QUE));
            DHMS href3 = DHMSHelper.narrow(ncRef.resolve_str(Constants.SHE));
            String result1 = href.addAppointment("MTLA270224", "Physician", 4);
            String result = href2.addAppointment("QUEA270224", "Physician", 4);
            //String result2 = href.removeAppointment("MTLA270224", "Physician");
//            String result3 = href2.listAppointmentAvailability("Physician");
            String test = href.bookAppointment("MTLP0001", "MTLA270224", "Physician");
            String result5 = href.swapAppointment("MTLP0001", "MTLA270224", "Physician", "QUEA270224", "Physician");
            System.out.println(result5);
//            System.out.println(test);
//            String result5 = href2.getAppointmentSchedule("QUEP0001");
//            String result4 = href.bookAppointment("MTLP0001", "QUEA270224", "Physician");
//            String result7 = href2.cancelAppointment("QUEP0001", "QUEA270224");
////            String result5 = href.getAppointmentSchedule("MTLP0001");
////            String result6 = href.cancelAppointment("MTLP0001", "MTLA270224");
//            System.out.println(result4);
//            System.out.println(result7);
        } catch (InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName | NotFound e) {
            e.printStackTrace();
        }

    }

}