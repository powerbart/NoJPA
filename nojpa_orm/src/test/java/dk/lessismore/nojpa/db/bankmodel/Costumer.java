package dk.lessismore.nojpa.db.bankmodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
public interface Costumer extends ModelObjectInterface {

    String getName();
    void setName(String name);

}
