package dk.lessismore.nojpa.db.model;

import dk.lessismore.nojpa.reflection.db.annotations.ModelObjectMethodListener;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 7/29/13
 * Time: 5:23 PM
 */
public interface MethodListened extends ModelObjectInterface {

    String getMyValue();
    @ModelObjectMethodListener(methodListener = MethodListened.Listener.class)
    void setMyValue(String myValue);


    public class Listener implements ModelObjectMethodListener.MethodListener {
        @Override
        public void preRun(Object mother, String methodName, Object[] args) {
            args[0] = args[0] + "_";
        }

        @Override
        public Object postRun(Object mother, String methodName, Object preResult, Object[] args) {
            return preResult;
        }
    }
}
