package dk.lessismore.reusable_v4.reflection.visitors;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

import java.util.Calendar;

public abstract class DbValueVisitor {

    // TODO Char Byte ...
    public abstract void visit(Integer value);
    public abstract void visit(Float value);
    public abstract void visit(Double value);
    public abstract void visit(Boolean value);
    public abstract void visit(String value);
    public abstract void visit(Calendar value);
    public abstract void visit(ModelObjectInterface value, Class<? extends ModelObjectInterface> modelInterface);
    public abstract void visit(ModelObjectInterface[] values, Class<? extends ModelObjectInterface> modelInterface);

    public void visit(int value) {
        visit((Integer) value);
    }

    public void visit(float value) {
        visit((Float) value);
    }

    public void visit(double value) {
        visit((Double) value);
    }

    public void visit(boolean value) {
        visit((Boolean) value);
    }


}
