package dk.lessismore.nojpa.net.link;


import dk.lessismore.nojpa.serialization.Serializer;

import java.net.Socket;
import java.io.IOException;

public class ServerLink extends AbstractLink {

    public ServerLink(Socket socket) throws IOException {
        this(socket, null);
    }

    public ServerLink(Socket socket, Serializer serializer) throws IOException {
        super(serializer);
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }
}
