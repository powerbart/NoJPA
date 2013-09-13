package dk.lessismore.nojpa.db.methodquery;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.util.List;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
public interface NList<C extends ModelObjectInterface> extends List<C> {
    long getNumberFound();
}
