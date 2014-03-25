package dk.lessismore.nojpa.rest;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 3/24/14
 * Time: 11:45 PM
 */
public interface ObjectLocator<T extends ModelObjectInterface> {
    public T get(String text);
}
