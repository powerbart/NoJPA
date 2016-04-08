package dk.lessismore.nojpa.masterworker.messages;

import dk.lessismore.nojpa.guid.GuidFactory;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 22-10-2010
 * Time: 12:22:16
 */
public class RunMethodRemoteBeanMessage {

    private String jobID;

    private String methodID = GuidFactory.getInstance().makeGuid();

    private String methodName;
    private Object[] args;

    public RunMethodRemoteBeanMessage() {
        this("init", null);
    }

    public RunMethodRemoteBeanMessage(String methodName, Object[] args){
        this.methodName = methodName;
        this.args = args;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getMethodID() {
        return methodID;
    }
}
