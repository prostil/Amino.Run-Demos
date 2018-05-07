package sapphire.oms;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import sapphire.common.AppObjectStub;
import sapphire.common.SapphireObjectID;
import sapphire.common.SapphireObjectNotFoundException;
import sapphire.common.SapphireReplicaID;
import sapphire.kernel.common.KernelOID;
import sapphire.kernel.common.KernelObjectNotFoundException;
import sapphire.kernel.common.KernelUtility;
import sapphire.kernel.server.KernelServer;
import sapphire.policy.SapphirePolicy.SapphireGroupPolicy;
import sapphire.policy.SapphirePolicy.SapphireServerPolicy;
import sapphire.runtime.EventHandler;

import org.json.JSONException;

/** OMSServer for tracking objects in Sapphire
 * 
 * @author iyzhang
 *
 */
public class OMSServerImpl implements OMSServer{
	   private static Logger logger = Logger.getLogger("sapphire.oms.OMSServerImpl");

	   private GlobalKernelObjectManager kernelObjectManager;
	   private AppObjectStub appEntryPoint = null;
	   private String appEntryClassName;
	   private KernelServerManager serverManager;
	   private SapphireObjectManager objectManager;

	   /** CONSTRUCTOR **/
       //TODO Should receive a List of servers
       public OMSServerImpl(String appEntryClassName) throws IOException, NotBoundException, JSONException {
    	   kernelObjectManager = new GlobalKernelObjectManager();
    	   serverManager = new KernelServerManager();
    	   objectManager = new SapphireObjectManager();
    	   this.appEntryClassName = appEntryClassName;
    	   logger.setLevel(Level.ALL);
		   ConsoleHandler handler = new ConsoleHandler();
		   handler.setLevel(Level.ALL);
		   handler.setFormatter(new SimpleFormatter());
		   logger.addHandler(handler);
       }

       /** KERNEL METHODS **/
       /**
        * Register new kernel object
        * @return a new unique kernel object ID
        */
       public KernelOID registerKernelObject(InetSocketAddress host) throws RemoteException {
           KernelOID oid = kernelObjectManager.register(host); 
           return oid;
       }
       
       /**
        * Register a new host for this kernel object. Used to move a kernel object
        */
       public void registerKernelObject(KernelOID oid, InetSocketAddress host) throws RemoteException, KernelObjectNotFoundException {
    	   logger.info("Registering new host for " + oid.toString() + " on " + host.toString());
    	   kernelObjectManager.register(oid, host);
       }

       /**
        * Register a new host for this kernel object. Used to move a kernel object
        */
       public void registerNewKernelObject(KernelOID oid, InetSocketAddress host) throws RemoteException, KernelObjectNotFoundException {
    	   logger.info("Registering new host from different OMS for " + oid.toString() + " on " + host.toString());
    	   kernelObjectManager.registerNewObject(oid, host);
       }
       
       /**
        * Find the host for a kernel object
        * @return the host IP address
        */
       public InetSocketAddress lookupKernelObject(KernelOID oid) throws RemoteException, KernelObjectNotFoundException {
    	   logger.info("Found host for " + oid.toString() + " host: " + kernelObjectManager.lookup(oid));
    	   return kernelObjectManager.lookup(oid);
       }

       @Override
       public void registerKernelServer(InetSocketAddress host) throws RemoteException, NotBoundException {
    	   serverManager.registerKernelServer(host);   		
       }

       @Override
       public void registerKernelServer(InetSocketAddress host, String region) throws RemoteException, NotBoundException {
    	   serverManager.registerKernelServer(host, region);
       }

	   /**
	    * Gets the list servers in the system
	    * 
        * @throws RemoteException 
        * @throws NumberFormatException 
        * @throws NotBoundException 
        */
       @Override
       public ArrayList<InetSocketAddress> getServers() throws NumberFormatException, RemoteException, NotBoundException {
    	   return serverManager.getServers();
       }

      /**
        * Gets the regions in the system
        * 
        * @throws RemoteException
        */
       @Override
       public ArrayList<String> getRegions() throws RemoteException {
   			// TODO Auto-generated method stub
   			return serverManager.getRegions();
   		}
	   
       
       /**
        * Gets a server in the specified region
        * @param region
        * @return
        * @throws RemoteException
        */
       @Override
       public InetSocketAddress getServerInRegion(String region) throws RemoteException {
    	   return serverManager.getHostNameInRegion(region);
       }
       
       /** APP METHODS **/
       
       /**
        * Starts the app on one of the servers and returns the App Object Stub
        * @throws RemoteException 
        */
       @Override
       public AppObjectStub getAppEntryPoint() throws RemoteException {
    	   if (appEntryPoint != null) {
    		   return appEntryPoint;
    	   } else {
				try {
					InetSocketAddress host = serverManager.getHostNameInRegion(serverManager.getRegions().get(0));
					KernelServer server = serverManager.getServer(host);
					logger.info("Starting app at the server: " + KernelUtility.getHostName(host));
					logger.info(" appEntryClassName: " + appEntryClassName);

					appEntryPoint = server.startApp(appEntryClassName);
					logger.info(" Successfully started " + appEntryClassName);
				} catch(Exception e) {
					e.printStackTrace();
					logger.severe(e.toString());
				}
    		   	return appEntryPoint;
    	   }
       }

       /**
        * Starts the app on one of the servers in the region and returns the App Object Stub
        * @throws RemoteException
        */
       @Override
       public AppObjectStub getAppEntryPoint(String region) throws RemoteException {
			try {
				InetSocketAddress host = serverManager.getHostNameInRegion(region);
				KernelServer server = serverManager.getServer(host);
				logger.info("Starting app at the server in region (" + region + ") : " + KernelUtility.getHostName(host));
				logger.info(" appEntryClassName: " + appEntryClassName);

				appEntryPoint = server.startApp(appEntryClassName);
				logger.info("Successfully started " + appEntryClassName + " at " + region + "(" + KernelUtility.getHostName(host) + ")");
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println();
				logger.severe(e.toString());
			}
			return appEntryPoint;
       }

//
//       /**
//        * Starts the app on one of the servers and returns the App Object Stub
//        * @throws RemoteException
//        */
//       @Override
//       public AppObjectStub getAppEntryPoint(Object object ) throws RemoteException {
//    	   if (appEntryPoint != null) {
//    		   return appEntryPoint;
//    	   } else {
//    		   	InetSocketAddress host = serverManager.getServerInRegion(serverManager.getRegions().get(0));
//    		   	KernelServer server = serverManager.getServer(host);
//    		   	appEntryPoint = server.startApp(appEntryClassName, object);
//    		   	return appEntryPoint;
//    	   }
//       }

       public static void main(String args[]) {

    	   if (args.length != 3) {
    		   System.out.println("Invalid arguments to OMS.");
    		   System.out.println("[IP] [port] [AppClassName]");
    		   return;
    	   }

           int port = 1099;
    	   try {
    		   port = Integer.parseInt(args[1]);
    	   } catch (NumberFormatException e) {
    		   System.out.println("Invalid arguments to OMS.");
    		   System.out.println("[IP] [port] [AppClassName]");
    		   return;
    	   }

    	   System.setProperty("java.rmi.server.hostname", args[0]);

    	   try {
    		   OMSServerImpl oms = new OMSServerImpl(args[2]);
    		   OMSServer omsStub = (OMSServer) UnicastRemoteObject.exportObject(oms, 0);
    		   Registry registry = LocateRegistry.createRegistry(port);
    		   registry.rebind("SapphireOMS", omsStub);

    		   logger.info("OMS ready");
    		   logger.info("Current canonical host: " + InetAddress.getLocalHost().getCanonicalHostName());
    		   logger.info("Current host name: " + InetAddress.getLocalHost().getHostName());
    		   logger.info("Current host address: " + InetAddress.getLocalHost().getHostAddress());
    	   	   for (Iterator<InetSocketAddress> it = oms.getServers().iterator(); it.hasNext();) {
        		   InetSocketAddress address = it.next();
				   System.out.println("   " + address.getHostName().toString() + ":" + address.getPort());
//        		   logger.fine("   " + address.getHostName().toString() + ":" + address.getPort());
        	   }
    	   } catch (Exception e) {
			   System.out.println("Server exception: " + e.toString());
			   e.printStackTrace();
    		   logger.severe("Server exception: " + e.toString());
    		   e.printStackTrace();
    	   }
		   logger.info("OMS main method complete.");
    }

	@Override
	public SapphireObjectID registerSapphireObject(EventHandler dispatcher)
			throws RemoteException {
		return objectManager.add(dispatcher);
	}

	@Override
	public SapphireReplicaID registerSapphireReplica(SapphireObjectID oid,
			EventHandler dispatcher) throws RemoteException, SapphireObjectNotFoundException {
		return objectManager.add(oid, dispatcher);
	}

	@Override
	public EventHandler getSapphireObjectDispatcher(SapphireObjectID oid)
			throws RemoteException, SapphireObjectNotFoundException {
		return objectManager.get(oid);
	}

	@Override
	public EventHandler getSapphireReplicaDispatcher(SapphireReplicaID rid)
			throws RemoteException, SapphireObjectNotFoundException {
		return objectManager.get(rid);
	}

}