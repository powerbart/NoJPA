package dk.lessismore.reusable_v4.masterworker.messages;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 21-10-2010
 * Time: 15:26:39
 */
public class NewRemoteBeanMessage {
    private Class sourceClazz;

    public NewRemoteBeanMessage(){}

    public NewRemoteBeanMessage(Class sourceClazz){
        this.sourceClazz = sourceClazz;
    }


    public Class getSourceClazz() {
        return sourceClazz;
    }

    public void setSourceClazz(Class clazz) {
        sourceClazz = clazz;
    }

}
