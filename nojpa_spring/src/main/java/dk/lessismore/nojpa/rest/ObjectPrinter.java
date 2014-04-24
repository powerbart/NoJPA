package dk.lessismore.nojpa.rest;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 3/24/14
 * Time: 11:43 PM
 */
public interface ObjectPrinter<T extends ModelObjectInterface> {
    String put(T t);
}
