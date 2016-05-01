package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.annotations.DbInline;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created by seb on 30/04/16.
 */
@DbInline
public interface Phone extends ModelObjectInterface {


    String getNumber();
    void setNumber(String number);

    double getFunnyD();
    void setFunnyD(double brand);

}
