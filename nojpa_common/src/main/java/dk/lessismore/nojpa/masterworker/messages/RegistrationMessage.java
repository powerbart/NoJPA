package dk.lessismore.nojpa.masterworker.messages;

import dk.lessismore.nojpa.masterworker.executor.Executor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class RegistrationMessage {

    private String[] knownClasses;
    private String hostname = "Unknown";
    private String folder = "UnknownF";


    public RegistrationMessage(List<? extends Class<? extends Executor>> classes) {
        this();
        knownClasses = new String[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            knownClasses[i] = classes.get(i).getName();
        }
    }

    public RegistrationMessage() {
        try {
            hostname = InetAddress.getLocalHost().toString();
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


    public static void main(String[] args) throws UnknownHostException {
        System.out.println(System.getenv("PWD"));
    }



}
