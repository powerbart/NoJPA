package dk.lessismore.nojpa.masterworker.messages;

import dk.lessismore.nojpa.masterworker.exceptions.WrappedErrorException;
import dk.lessismore.nojpa.masterworker.exceptions.MasterWorkerException;
import dk.lessismore.nojpa.serialization.Serializer;

public class JobResultMessage<O> extends JobRelatedMessage {

    private String exception;
    private String result;
    private MasterWorkerException masterException;

    public JobResultMessage() {
    }

    public JobResultMessage(String jobID) {
        super(jobID);
    }

    public RuntimeException getException(Serializer serializer) {
        return serializer.unserialize(exception);
    }

    public void setException(Exception exception, Serializer serializer) {
        if (exception instanceof RuntimeException) {
            this.exception = serializer.serialize(exception);
        } else {
            this.exception = serializer.serialize(new WrappedErrorException(exception));
        }
        result = null;
    }

    public O getResult(Serializer serializer) {
        return (O)serializer.unserialize(result);
    }

    public void setResult(O result, Serializer serializer) {
        this.result = serializer.serialize(result);
        exception = null;
    }

    public void setMasterException(MasterWorkerException masterException) {
        this.masterException = masterException;
        this.result = null;
    }

    public boolean hasException() {
        return exception != null;
    }

    public RuntimeException getMasterException() {
        return masterException;
    }

    public boolean hasMasterException() {
        return masterException != null;
    }


    public String toString(){
        int i = result == null ? -1 : result.length();
        return super.toString() + " exception("+ exception +") result("+ (i > 100 ? result.substring(0, 100) : null) +")";
    }


}
