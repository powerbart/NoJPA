package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.annotations.IgnoreFromTableCreation;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created by IntelliJ IDEA.
 * User: niakoi
 * Date: 4/28/11
 * Time: 5:48 PM
 */
@IgnoreFromTableCreation
public interface ToBeIgnoredTable extends ModelObjectInterface {
}
