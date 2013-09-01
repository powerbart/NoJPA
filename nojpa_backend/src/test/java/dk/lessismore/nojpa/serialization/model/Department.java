package dk.lessismore.nojpa.serialization.model;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

public interface Department extends ModelObjectInterface {

    public String getName();
    public void setName(String name);

    public Employee getChef();
    public void setChef(Employee chef);
}
