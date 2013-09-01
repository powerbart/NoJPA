package dk.lessismore.nojpa.net.link;

import dk.lessismore.nojpa.concurrency.WaitForValue;
import dk.lessismore.nojpa.utils.Pair;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class LinkTest {

    private final String serverHost = "localhost";

    @Test
    public void simpleSendReceiveTest() throws IOException, InterruptedException {
        final int serverPort = 1337;

        Thread serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(serverPort);
                    ServerLink serverLink = acceptConnection(serverSocket);
                    Pair<String, Double> p = (Pair<String, Double>) serverLink.read();
                    assertEquals(new Pair<String, Double>("PI", 3.14), p);
                    serverLink.write(null);
                } catch (IOException e) {
                    assertTrue(e.getMessage(), false);
                }
            }
        });

        Thread clientThread = new Thread(new Runnable() {
            public void run() {
                try {
                    ClientLink clientLink = new ClientLink(serverHost, serverPort);
                    clientLink.write(new Pair<String, Double>("PI", 3.14));
                    Object o = clientLink.read();
                    assertNull(o);
                } catch (IOException e) {
                    assertTrue(e.getMessage(), false);
                }

            }
        });

        // serverThread.start();
        // clientThread.start();
        // serverThread.join();
        // clientThread.join();
    }

    @Test
    public void sendWithTimeoutTest() throws IOException, InterruptedException {
        final int serverPort = 1338;
        final WaitForValue<Socket> clientSocket = new WaitForValue<Socket>();

        Thread clientThread = new Thread(new Runnable() {

            public void run() {
                try {
                    ClientLink clientLink = new ClientLink(serverHost, serverPort);
                    clientSocket.setValue(clientLink.socket);
                    clientLink.writeWithTimeout(3.14, 1000);
                    Object o = clientLink.read();
                    assertNull(o);
                } catch (IOException e) {
                    assertTrue(e.getMessage(), false);
                }
            }
        });

        Thread serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(serverPort);
                    ServerLink serverLink = acceptConnection(serverSocket);
                    Double pi = (Double) serverLink.read();
                    assertEquals(new Double(3.14), pi);
                    serverLink.writeWithTimeout(null, 1000);

                    // We will now write a message to the client, larger than the receivers OS receive buffer.
                    // This will cause the send operation to block as the client is not reading the message.
                    // This should trigger a timeout.
                    int clientBufferSize = clientSocket.getValue().getReceiveBufferSize();
                    int[] overflowMessage = new int[clientBufferSize];
                    serverLink.writeWithTimeout(overflowMessage, 1000);
                    assertTrue("The write above was supposed throw a WriteTimeoutException", false);
                } catch (WriteTimeoutException e) {
                } catch (IOException e) {
                    assertTrue(e.getMessage(), false);
                }
            }
        });


        // serverThread.start();
        // clientThread.start();
        // serverThread.join();
        // clientThread.join();
    }

    private ServerLink acceptConnection(ServerSocket serverSocket) throws IOException {
        Socket socket;
        socket = serverSocket.accept();
        socket.setKeepAlive(true);
        socket.setSoTimeout(1000 * 180);
        socket.setTcpNoDelay(true);
        return new ServerLink(socket);
    }
}
