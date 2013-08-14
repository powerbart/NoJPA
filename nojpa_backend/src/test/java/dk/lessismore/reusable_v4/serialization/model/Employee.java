package dk.lessismore.reusable_v4.serialization.model;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

public interface Employee extends ModelObjectInterface {

    public String getName();
    public void setName(String name);

    public Department[] getDepartments();
    public void setDepartments(Department[] departments);
}
