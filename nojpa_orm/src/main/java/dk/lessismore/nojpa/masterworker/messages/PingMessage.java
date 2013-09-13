package dk.lessismore.nojpa.masterworker.messages;

/**
 * Created : by IntelliJ IDEA.
 * User: Philip August Lerche
 * Date: 2007-06-16
 * Time: 01:38:07
 * To change this template use File | Settings | File Templates.
 */
public class PingMessage {

    public String timeInMillis = null;

    public PingMessage(){ timeInMillis = "" + System.currentTimeMillis(); }

    public PingMessage(String timeInMillis){
        this.timeInMillis = timeInMillis;
    }
}
