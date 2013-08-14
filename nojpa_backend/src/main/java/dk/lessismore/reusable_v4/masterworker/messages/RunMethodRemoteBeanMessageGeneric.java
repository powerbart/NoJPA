package dk.lessismore.reusable_v4.masterworker.messages;

import dk.lessismore.reusable_v4.guid.GuidFactory;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

/**
 * Created by IntelliJ IDEA.
 * User: niakoi
 * Date: 1/19/11
 * Time: 12:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class RunMethodRemoteBeanMessageGeneric <T extends ModelObjectInterface> {
    private String jobID;

    private String methodID = GuidFactory.getInstance().makeGuid();

    private String methodName;
    private T[] args;

    public RunMethodRemoteBeanMessageGeneric() {
        this("init", null);
    }

    public RunMethodRemoteBeanMessageGeneric(String methodName, T[] args){
        this.methodName = methodName;
        this.args = args;
    }

    public String getMethodName() {
        return methodName;
    }

    public T[] getArgs() {
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
