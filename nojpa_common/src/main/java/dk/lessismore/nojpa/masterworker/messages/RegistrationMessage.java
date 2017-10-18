package dk.lessismore.nojpa.masterworker.messages;

import dk.lessismore.nojpa.masterworker.bean.RemoteBeanInterface;
import dk.lessismore.nojpa.masterworker.executor.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class RegistrationMessage {


    private String[] knownClasses;
    private String hostname = "Unknown";
    private String folder = "UnknownF";


    public RegistrationMessage(Class<? extends RemoteBeanInterface> remoteBeanClass, List<Class<? extends Executor>> supportedExecutors) {
        this();
        knownClasses = new String[supportedExecutors.size() + (remoteBeanClass != null ? 1 :0)];
        for (int i = 0; i < supportedExecutors.size(); i++) {
            knownClasses[i] = supportedExecutors.get(i).getName();
        }
        if(remoteBeanClass != null){
            knownClasses[knownClasses.length - 1] = remoteBeanClass.getName();
        }
        for (int i = 0; i < knownClasses.length; i++) {
            System.out.println("Will registrate classes["+ i +"]: " + knownClasses[i]);

        }
    }

    public RegistrationMessage() {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(hostname.equals("Unknown") || hostname.equals("localhost")){
            try {
                hostname = System.getenv("HOST");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        try {
            folder = System.getenv("PWD");
            if(folder != null && folder.indexOf("/") != -1){
                folder = folder.substring(folder.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getHostname() {
        return hostname;
    }

    public String getFolder() {
        return folder;
    }

    public String[] getKnownClasses() {
        return knownClasses;
    }

    public void setKnownClasses(String[] knownClasses) {
        this.knownClasses = knownClasses;
    }


}
