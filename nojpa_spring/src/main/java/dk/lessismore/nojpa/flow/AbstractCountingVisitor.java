package dk.lessismore.nojpa.flow;

import dk.lessismore.nojpa.reflection.db.DbObjectVisitor;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created on 6/7/16.
 */
public abstract class AbstractCountingVisitor<T extends ModelObjectInterface> implements DbObjectVisitor<T> {

    private int counter;

    @Override
    public void setDone(boolean b) {
    }

    @Override
    public boolean getDone() {
        return false;
    }

    @Override
    public void visit(T t) {
        counter++;
        process(t);
    }

    public abstract void process(T t);

    public int getCounter() {
        return counter;
    }
}