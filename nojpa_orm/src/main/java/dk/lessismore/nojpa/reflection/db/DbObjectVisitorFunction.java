package dk.lessismore.nojpa.reflection.db;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created on 3/14/17.
 */
@FunctionalInterface
public interface DbObjectVisitorFunction <K extends ModelObjectInterface> {

    void visit(K m);

}
