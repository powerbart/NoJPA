package dk.lessismore.reusable_v4.masterworker.messages.observer;

import java.util.Calendar;
import java.util.List;

public class UpdateMessage {

    private List<ObserverJobMessage> observerJobMessages;
    private List<ObserverWorkerMessage> observerWorkerMessages;
    private Calendar started;

    public List<ObserverJobMessage> getObserverJobMessages() {
        return observerJobMessages;
    }

    public void setObserverJobMessages(List<ObserverJobMessage> observerJobMessages) {
        this.observerJobMessages = observerJobMessages;
    }

    public List<ObserverWorkerMessage> getObserverWorkerMessages() {
        return observerWorkerMessages;
    }

    public void setObserverWorkerMessages(List<ObserverWorkerMessage> observerWorkerMessages) {
        this.observerWorkerMessages = observerWorkerMessages;
    }

    public Calendar getStarted() {
        return started;
    }

    public void setStarted(Calendar started) {
        this.started = started;
    }
}
