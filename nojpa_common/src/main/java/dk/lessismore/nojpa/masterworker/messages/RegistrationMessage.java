package dk.lessismore.nojpa.masterworker.messages;

import dk.lessismore.nojpa.masterworker.executor.Executor;

import java.util.List;

public class RegistrationMessage {

    private String[] knownClasses;

    public RegistrationMessage(List<? extends Class<? extends Executor>> classes) {
        knownClasses = new String[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            knownClasses[i] = classes.get(i).getName();
        }
    }

    public RegistrationMessage() {
    }

    public String[] getKnownClasses() {
        return knownClasses;
    }

    public void setKnownClasses(String[] knownClasses) {
        this.knownClasses = knownClasses;
    }
}
