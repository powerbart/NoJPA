package dk.lessismore.reusable_v4.db.testmodel;

import dk.lessismore.reusable_v4.reflection.db.annotations.IndexField;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

public interface Address extends ModelObjectInterface {

    String getStreet();
    void setStreet(String street);

    String getCity();
    void setCity(String city);

    String getArea();
    void setArea(String area);

    @IndexField
    int getZip();
    void setZip(int zip);
}