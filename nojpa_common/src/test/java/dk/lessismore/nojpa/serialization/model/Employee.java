package dk.lessismore.nojpa.serialization.model;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

public interface Employee extends ModelObjectInterface {

    public String getName();
    public void setName(String name);

    public Department[] getDepartments();
    public void setDepartments(Department[] departments);
}
