package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.annotations.DbInline;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created by seb on 19/07/16.
 */
@DbInline
public interface XNlpValue extends ModelObjectInterface {



    // nlped
    @SearchField
    double getNlpVeryNegative();
    void setNlpVeryNegative(double veryNegative);

    @SearchField
    double getNlpNegative();
    void setNlpNegative(double negative);

    @SearchField
    double getNlpNeutral();
    void setNlpNeutral(double neutral);

    @SearchField
    double getNlpPositive();
    void setNlpPositive(double positive);

    @SearchField
    double getNlpVeryPositive();
    void setNlpVeryPositive(double veryPositive);

}
