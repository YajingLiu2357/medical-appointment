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
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant
            MontrealServer montrealServer = new MontrealServer();

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(montrealServer);
            DHMS href = DHMSHelper.narrow(ref);

            org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent path[] = ncRef.to_name( "MTL-SERVER" );
            ncRef.rebind(path, href);

            QuebecServer quebecServer = new QuebecServer();
            org.omg.CORBA.Object ref2 = rootpoa.servant_to_reference(quebecServer);
            DHMS href2 = DHMSHelper.narrow(ref2);
            NameComponent path2[] = ncRef.to_name( "QUE-SERVER" );
            ncRef.rebind(path2, href2);

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