package dk.lessismore.reusable_v4.db.model;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import dk.lessismore.reusable_v4.reflection.db.annotations.DbStrip;

public interface Person extends ModelObjectInterface {
    public String getName();

    @DbStrip(stripItSoft = false)
    public void setName(String value);
    public int getAge();
    public void setAge(int value);


}
