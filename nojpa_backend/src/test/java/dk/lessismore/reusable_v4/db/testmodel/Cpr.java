package dk.lessismore.reusable_v4.db.testmodel;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

public interface Cpr extends ModelObjectInterface {

    String getNumber();
    void setNumber(String number);
}