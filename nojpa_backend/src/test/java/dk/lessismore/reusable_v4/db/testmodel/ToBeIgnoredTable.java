package dk.lessismore.reusable_v4.db.testmodel;

import dk.lessismore.reusable_v4.reflection.db.annotations.IgnoreFromTableCreation;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

/**
 * Created by IntelliJ IDEA.
 * User: niakoi
 * Date: 4/28/11
 * Time: 5:48 PM
 */
@IgnoreFromTableCreation
public interface ToBeIgnoredTable extends ModelObjectInterface {
}
