import DHMSApp.DHMS;
import DHMSApp.DHMSHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server {

    public static void main(String args[]) {
        try{
            ORB orb = ORB.init(args, null);
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            MontrealServer montrealServer = new MontrealServer();
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(montrealServer);
            DHMS href = DHMSHelper.narrow(ref);
            org.omg.CORBA.Object objRef =  orb.resolve_initial_references(Constants.NAME_SERVICE);
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            NameComponent path[] = ncRef.to_name(Constants.MTL);
            ncRef.rebind(path, href);

            QuebecServer quebecServer = new QuebecServer();
            org.omg.CORBA.Object ref2 = rootpoa.servant_to_reference(quebecServer);
            DHMS href2 = DHMSHelper.narrow(ref2);
            NameComponent path2[] = ncRef.to_name(Constants.QUE);
            ncRef.rebind(path2, href2);

            SherbrookeServer sherbrookeServer = new SherbrookeServer();
            org.omg.CORBA.Object ref3 = rootpoa.servant_to_reference(sherbrookeServer);
            DHMS href3 = DHMSHelper.narrow(ref3);
            NameComponent path3[] = ncRef.to_name(Constants.SHE);
            ncRef.rebind(path3, href3);


            System.out.println("Server ready and waiting ...");

            // wait for invocations from clients
            orb.run();
        }

        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

        System.out.println("Exiting ...");

    }
}