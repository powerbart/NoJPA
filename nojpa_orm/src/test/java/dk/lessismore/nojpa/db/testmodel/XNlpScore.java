package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.annotations.DbInline;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created by seb on 19/07/16.
 */
@DbInline
public interface XNlpScore extends ModelObjectInterface {


    @SearchField
    double getPos();
    void setPos(double pos);

    @SearchField
    double getNeg();
    void setNeg(double neg);

}
