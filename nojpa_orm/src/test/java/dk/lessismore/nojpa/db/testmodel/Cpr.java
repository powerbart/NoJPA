package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

public interface Cpr extends ModelObjectInterface {

    String getNumber();
    void setNumber(String number);
}