package dk.lessismore.nojpa.db.model;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;

public interface Person extends ModelObjectInterface {
    public String getName();

    @DbStrip(stripItSoft = false)
    public void setName(String value);
    public int getAge();
    public void setAge(int value);


}
