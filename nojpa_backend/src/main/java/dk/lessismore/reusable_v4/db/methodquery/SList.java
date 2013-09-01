package dk.lessismore.reusable_v4.db.methodquery;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public interface SList<C extends ModelObjectInterface> extends List<C> {
    long getNumberFound();
}
