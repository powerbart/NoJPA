package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.properties.PropertiesProxy;
import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.resources.PropertyService;

import java.io.IOException;
import java.net.ServerSocket;

public class Master {

    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);

    /**
     * Run Master listening on ports specified in property file.
     * @param args Not used.
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        PropertyResources resources = PropertyService.getInstance().getPropertyResources(Master.class);
        System.out.println("resources = " + resources.getString("clientPort"));

        final MasterServer server = new MasterServer();
        // TODO move sockets inside the link api
        final ServerSocket clientSocket = new ServerSocket(properties.getClientPort());
        final ServerSocket workerSocket = new ServerSocket(properties.getWorkerPort());
        final ServerSocket observerSocket = new ServerSocket(properties.getObserverPort());
        clientSocket.setSoTimeout(0);
        workerSocket.setSoTimeout(0);
        observerSocket.setSoTimeout(0);
        System.out.format("Master is now running on %s using client port %s, worker port %s and observer port %s\n",
                properties.getHost(), properties.getClientPort(), properties.getWorkerPort(), properties.getObserverPort());

        Thread clientAcceptThread = new Thread(new Runnable() {
            public void run() {
                while(true) server.acceptClientConnection(clientSocket);
            }
        });
        Thread workerAcceptThread = new Thread(new Runnable() {
            public void run() {
                while(true) server.acceptWorkerConnection(workerSocket);
            }
        });
        Thread observerAcceptThread = new Thread(new Runnable() {
            public void run() {
                while(true) server.acceptObserverConnection(observerSocket);
            }
        });

        ObserverNotifierThread observerNotifierThread = new ObserverNotifierThread(server);

        clientAcceptThread.start();
        workerAcceptThread.start();
        observerAcceptThread.start();
        observerNotifierThread.start();

        clientAcceptThread.join();
        workerAcceptThread.join();
        observerAcceptThread.join();

        observerNotifierThread.stopThread();
        observerNotifierThread.join();
    }
}
