package dk.lessismore.nojpa.net.link;

import dk.lessismore.nojpa.serialization.Serializer;

import java.net.Socket;
import java.io.IOException;

public class ClientLink extends AbstractLink {

    public ClientLink(String serverName, int port) throws IOException {
        this(serverName, port, null);
    }

    public ClientLink(String serverName, int port, Serializer serializer) throws IOException {
        super(serializer);
        socket = new Socket(serverName, port);
        socket.setKeepAlive(true);
        socket.setSoTimeout(0); // This implies that a read call will block forever.
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

}
