import DHMSApp.DHMS;
import DHMSApp.DHMSHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.concurrent.ExecutorService;

public class TestMultipleThreads {
    static DHMS hrefMTL;
    static DHMS hrefQUE;
    static DHMS hrefSHE;
    static Patient patientMTL;
    static Patient patientQUE;
    public static void main (String args[]){
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Constants.NAME_SERVICE);
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            hrefMTL = DHMSHelper.narrow(ncRef.resolve_str(Constants.MTL));
            hrefQUE = DHMSHelper.narrow(ncRef.resolve_str(Constants.QUE));
            hrefSHE = DHMSHelper.narrow(ncRef.resolve_str(Constants.SHE));
            Admin adminMTL = new Admin("MTLA0001", hrefMTL);
            adminMTL.addAppointment("MTLA080224", "Physician", 4);
            Admin adminQUE = new Admin("QUEA0001", hrefQUE);
            adminQUE.addAppointment("QUEA100224", "Physician", 1);
            patientMTL = new Patient("MTLP0001", hrefMTL);
            patientQUE = new Patient("QUEP0001", hrefQUE);
            patientMTL.bookAppointment("MTLP0001", "MTLA080224", "Physician");
            ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);
            executor.execute(new BookAppointment());
            executor.execute(new SwapAppointment());
        } catch (InvalidName | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName | NotFound e) {
            e.printStackTrace();
        }
    }
    public static class BookAppointment implements Runnable{
        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            patientQUE.bookAppointment("QUEP0001", "QUEA100224", "Physician");
        }
    }
    public static class SwapAppointment implements Runnable{
        @Override
        public void run() {
            patientMTL.swapAppointment("MTLP0001", "MTLA080224", "Physician", "QUEA100224", "Physician");
        }
    }
}
